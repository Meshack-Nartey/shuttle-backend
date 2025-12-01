package com.shuttlebackend.websocket.buffer;

import com.shuttlebackend.dtos.LocationBroadcastDto;
import com.shuttlebackend.dtos.StudentActiveShuttleDto;
import com.shuttlebackend.entities.LocationUpdate;
import com.shuttlebackend.entities.Shuttle;
import com.shuttlebackend.repositories.LocationUpdateRepository;
import com.shuttlebackend.repositories.ShuttleRepository;
import com.shuttlebackend.repositories.DriverSessionRepository;
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
    private final ShuttleRepository shuttleRepository;
    private final DriverSessionRepository sessionRepository;
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

                    // Additionally publish student-facing active shuttle DTO for this shuttle's school
                    try {
                        Shuttle shuttle = shuttleRepository.findById(shuttleId).orElse(null);
                        if (shuttle != null) {
                            String routeName = null;
                            var sessionOpt = sessionRepository.findActiveByShuttleId(shuttleId);
                            if (sessionOpt.isPresent() && sessionOpt.get().getRoute() != null) {
                                routeName = sessionOpt.get().getRoute().getRouteName();
                            }

                            StudentActiveShuttleDto studentDto = new StudentActiveShuttleDto(
                                    shuttle.getExternalId() != null ? shuttle.getExternalId() : shuttle.getLicensePlate(),
                                    latest.getLatitude() != null ? latest.getLatitude().doubleValue() : null,
                                    latest.getLongitude() != null ? latest.getLongitude().doubleValue() : null,
                                    routeName,
                                    shuttle.getStatus(),
                                    latest.getCreatedAt()
                            );

                            String schoolExternal = shuttle.getSchool() != null ? shuttle.getSchool().getExternalId() : "unknown";
                            String studentTopic = "/topic/student/shuttles/" + schoolExternal;
                            messagingTemplate.convertAndSend(studentTopic, List.of(studentDto));
                        }
                    } catch (Exception ex) {
                        // don't fail flush if student publish fails
                    }
                });
            });
        }
    }
}
