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

import java.util.*;

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

            // since School.externalId is removed, group by schoolId instead
            Map<Long, List<StudentActiveShuttleDto>> bySchool = new HashMap<>();

            for (Shuttle s : active) {

                Integer shuttleId = s.getId();

                // latest location
                LocationUpdate latestLoc =
                        locationRepo.findTop1ByShuttle_IdOrderByCreatedAtDesc(shuttleId)
                                .orElse(null);

                // active session for route
                DriverSession session =
                        sessionRepo.findActiveByShuttleId(shuttleId)
                                .orElse(null);

                String routeName =
                        (session != null && session.getRoute() != null)
                                ? session.getRoute().getRouteName()
                                : null;

                // KEEP Shuttle.externalId (fallback to license plate)
                String shuttleIdentifier =
                        s.getExternalId() != null ? s.getExternalId() : s.getLicensePlate();

                StudentActiveShuttleDto dto = new StudentActiveShuttleDto(
                        shuttleIdentifier,
                        latestLoc != null ? latestLoc.getLatitude().doubleValue() : null,
                        latestLoc != null ? latestLoc.getLongitude().doubleValue() : null,
                        routeName,
                        s.getStatus(),
                        latestLoc != null ? latestLoc.getCreatedAt() : null
                );

                // FIX: use schoolId instead of removed schoolExternalId
                Long schoolId = (s.getSchool() != null)
                        ? s.getSchool().getId()
                        : -1L;

                bySchool.computeIfAbsent(schoolId, k -> new ArrayList<>()).add(dto);
            }

            // broadcast per school ID
            for (Map.Entry<Long, List<StudentActiveShuttleDto>> e : bySchool.entrySet()) {
                String topic = "/topic/student/shuttles/" + e.getKey();
                messagingTemplate.convertAndSend(topic, e.getValue());
            }

        } catch (Exception ex) {
            logger.warn("Failed to publish active shuttles: {}", ex.getMessage());
        }
    }
}
