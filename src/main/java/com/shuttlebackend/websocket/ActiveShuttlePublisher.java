package com.shuttlebackend.websocket;

import com.shuttlebackend.dtos.StudentActiveShuttleDto;
import com.shuttlebackend.entities.DriverSession;
import com.shuttlebackend.entities.LocationUpdate;
import com.shuttlebackend.entities.Shuttle;
import com.shuttlebackend.repositories.DriverSessionRepository;
import com.shuttlebackend.repositories.LocationUpdateRepository;
import com.shuttlebackend.repositories.ShuttleRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ActiveShuttlePublisher {

    private final ShuttleRepository shuttleRepo;
    private final DriverSessionRepository sessionRepo;
    private final LocationUpdateRepository locationRepo;
    private final SimpMessagingTemplate messagingTemplate;
    private final Logger logger = LoggerFactory.getLogger(ActiveShuttlePublisher.class);

    public void publishActiveShuttles() {
        try {
            if (shuttleRepo.countByStatus("Active") == 0) return;
            List<Shuttle> active = shuttleRepo.findAllByStatus("Active");
            if (active == null || active.isEmpty()) return;

            // group payloads by school external id
            Map<String, List<StudentActiveShuttleDto>> bySchool = new HashMap<>();

            for (Shuttle s : active) {
                Integer shuttleId = s.getId();
                // latest location
                LocationUpdate latestLoc = locationRepo.findTop1ByShuttle_IdOrderByCreatedAtDesc(shuttleId).orElse(null);
                // active session to get route name
                DriverSession session = sessionRepo.findActiveByShuttleId(shuttleId).orElse(null);
                String routeName = session != null && session.getRoute() != null ? session.getRoute().getRouteName() : null;

                StudentActiveShuttleDto dto = new StudentActiveShuttleDto(
                        s.getExternalId() != null ? s.getExternalId() : s.getLicensePlate(),
                        latestLoc != null ? latestLoc.getLatitude().doubleValue() : null,
                        latestLoc != null ? latestLoc.getLongitude().doubleValue() : null,
                        routeName,
                        s.getStatus(),
                        latestLoc != null ? latestLoc.getCreatedAt() : null
                );

                String schoolExternal = s.getSchool() != null ? s.getSchool().getExternalId() : "unknown";
                bySchool.computeIfAbsent(schoolExternal, k -> new ArrayList<>()).add(dto);
            }

            // broadcast per school
            for (Map.Entry<String, List<StudentActiveShuttleDto>> e : bySchool.entrySet()) {
                String topic = "/topic/student/shuttles/" + e.getKey();
                messagingTemplate.convertAndSend(topic, e.getValue());
            }
        } catch (Exception ex) {
            logger.warn("Failed to publish active shuttles: {}", ex.getMessage());
        }
    }
}
