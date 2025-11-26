package com.shuttlebackend.websocket.buffer;

import com.shuttlebackend.dtos.LocationBroadcastDto;
import com.shuttlebackend.entities.LocationUpdate;
import com.shuttlebackend.repositories.LocationUpdateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LocationBufferService {

    private final LocationUpdateRepository repo;
    private final SimpMessagingTemplate messagingTemplate;

    // in-memory queue for incoming updates
    private final ConcurrentLinkedQueue<LocationUpdate> queue = new ConcurrentLinkedQueue<>();

    // push into in-memory buffer
    public void bufferUpdate(LocationUpdate lu) {
        if (lu.getCreatedAt() == null) lu.setCreatedAt(Instant.now());
        queue.add(lu);
    }

    // scheduled flush: every 2 seconds (adjust as needed)
    @Scheduled(fixedDelay = 2000, initialDelay = 2000)
    @Transactional
    public void flush() {
        if (queue.isEmpty()) return;

        List<LocationUpdate> batch = new ArrayList<>();
        LocationUpdate lu;
        while ((lu = queue.poll()) != null) {
            batch.add(lu);
        }

        if (!batch.isEmpty()) {
            // persist batch
            repo.saveAll(batch);

            // group by shuttle id and pick the latest update per shuttle
            Map<Integer, Optional<LocationUpdate>> latestByShuttleOpt = batch.stream()
                    .collect(Collectors.groupingBy(
                            luu -> luu.getShuttle().getId(),
                            Collectors.maxBy(Comparator.comparing(LocationUpdate::getCreatedAt))
                    ));

            // For each shuttle, if present, broadcast the latest location DTO
            latestByShuttleOpt.forEach((shuttleId, optLu) -> {
                optLu.ifPresent(latest -> {
                    LocationBroadcastDto payload = new LocationBroadcastDto(
                            shuttleId,
                            latest.getLatitude().doubleValue(),
                            latest.getLongitude().doubleValue(),
                            latest.getCreatedAt()
                    );
                    String topic = "/topic/shuttle/" + shuttleId + "/location";
                    messagingTemplate.convertAndSend(topic, payload);
                });
            });
        }
    }
}
