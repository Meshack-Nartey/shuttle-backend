package com.shuttlebackend.websocket.buffer;

import com.shuttlebackend.dtos.LocationBroadcastDto;
import com.shuttlebackend.dtos.ShuttleRealtimeDto;
import com.shuttlebackend.dtos.StudentActiveShuttleDto;
import com.shuttlebackend.entities.LocationUpdate;
import com.shuttlebackend.entities.Shuttle;
import com.shuttlebackend.entities.RouteStop;
import com.shuttlebackend.repositories.LocationUpdateRepository;
import com.shuttlebackend.repositories.ShuttleRepository;
import com.shuttlebackend.repositories.DriverSessionRepository;
import com.shuttlebackend.repositories.RouteStopRepository;
import com.shuttlebackend.services.DirectionDetectorService;
import com.shuttlebackend.services.SpeedEstimatorService;
import com.shuttlebackend.services.PolylineDistanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class LocationBufferService {

    private final LocationUpdateRepository repo;
    private final ShuttleRepository shuttleRepository;
    private final DriverSessionRepository sessionRepository;
    private final RouteStopRepository routeStopRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final DirectionDetectorService directionDetector;
    private final SpeedEstimatorService speedEstimator;
    private final PolylineDistanceService polyService;

    private final ConcurrentLinkedQueue<LocationUpdate> queue = new ConcurrentLinkedQueue<>();

    public void bufferUpdate(LocationUpdate lu) {
        if (lu.getCreatedAt() == null) lu.setCreatedAt(Instant.now());
        queue.add(lu);
    }

    @Scheduled(fixedDelay = 2000, initialDelay = 2000)
    @Transactional
    public void flush() {

        if (queue.isEmpty()) return;

        List<LocationUpdate> batch = new ArrayList<>();
        LocationUpdate lu;

        while ((lu = queue.poll()) != null) {
            batch.add(lu);
        }

        if (batch.isEmpty()) return;

        repo.saveAll(batch);

        Map<Integer, Optional<LocationUpdate>> latestByShuttleOpt =
                batch.stream()
                        .collect(Collectors.groupingBy(
                                u -> u.getShuttle().getId(),
                                Collectors.maxBy(Comparator.comparing(LocationUpdate::getCreatedAt))
                        ));

        latestByShuttleOpt.forEach((shuttleId, optLu) -> {
            optLu.ifPresent(latest -> {

                // DRIVER broadcast
                LocationBroadcastDto payload = new LocationBroadcastDto(
                        shuttleId,
                        latest.getLatitude().doubleValue(),
                        latest.getLongitude().doubleValue(),
                        latest.getCreatedAt()
                );

                String driverTopic = "/topic/shuttle/" + shuttleId + "/location";
                messagingTemplate.convertAndSend(driverTopic, payload);

                try {
                    Shuttle shuttle = shuttleRepository.findById(shuttleId).orElse(null);
                    if (shuttle == null) return;

                    String routeName = null;
                    var sessionOpt = sessionRepository.findActiveByShuttleId(shuttleId);
                    if (sessionOpt.isPresent() && sessionOpt.get().getRoute() != null) {
                        routeName = sessionOpt.get().getRoute().getRouteName();
                    }

                    // FIX: Removed externalId → using licensePlate
                    StudentActiveShuttleDto studentDto = new StudentActiveShuttleDto(
                            shuttle.getLicensePlate(),
                            latest.getLatitude().doubleValue(),
                            latest.getLongitude().doubleValue(),
                            routeName,
                            shuttle.getStatus(),
                            latest.getCreatedAt()
                    );

                    // FIX: removed school.externalId → using schoolId
                    Long schoolId = (shuttle.getSchool() != null)
                            ? shuttle.getSchool().getId()
                            : -1L;

                    String studentTopic = "/topic/student/shuttles/" + schoolId;
                    messagingTemplate.convertAndSend(studentTopic, List.of(studentDto));

                    // REALTIME enriched info
                    String direction = "stationary";
                    double speedKph = speedEstimator.estimateSpeedKph(shuttleId);
                    String nextStopName = null;

                    if (sessionOpt.isPresent() && sessionOpt.get().getRoute() != null) {

                        var session = sessionOpt.get();
                        var route = session.getRoute();

                        var dirEnum = directionDetector.detectDirection(
                                shuttleId,
                                route.getId(),
                                latest.getLatitude().doubleValue(),
                                latest.getLongitude().doubleValue(),
                                route.getPolylineForward(),
                                route.getPolylineBackward()
                        );

                        direction = dirEnum == null ? "stationary" : dirEnum.name().toLowerCase();

                        List<RouteStop> stops =
                                routeStopRepository.findByRoute_IdOrderByStopOrderAsc(route.getId());

                        if (stops != null && !stops.isEmpty()) {

                            List<List<Double>> usedPoly =
                                    (dirEnum == null || dirEnum == DirectionDetectorService.Direction.UNKNOWN)
                                            ? route.getPolylineForward()
                                            : (dirEnum == DirectionDetectorService.Direction.FORWARD
                                            ? route.getPolylineForward()
                                            : route.getPolylineBackward());

                            if (usedPoly != null && !usedPoly.isEmpty()) {

                                List<RouteStop> ordered = new ArrayList<>(stops);
                                if (dirEnum == DirectionDetectorService.Direction.BACKWARD) {
                                    Collections.reverse(ordered);
                                }

                                var shuttleNearest = polyService.nearestOnPolyline(
                                        usedPoly,
                                        latest.getLatitude().doubleValue(),
                                        latest.getLongitude().doubleValue()
                                );

                                int shuttleSeg = shuttleNearest.segmentIndex();
                                double shuttleT = shuttleNearest.proj().t;

                                double bestDist = Double.POSITIVE_INFINITY;
                                RouteStop bestStop = null;

                                for (RouteStop s : ordered) {
                                    var sn = polyService.nearestOnPolyline(
                                            usedPoly,
                                            s.getLatitude().doubleValue(),
                                            s.getLongitude().doubleValue()
                                    );
                                    int sSeg = sn.segmentIndex();
                                    double sT = sn.proj().t;

                                    double dAlong = polyService.distanceAlong(
                                            usedPoly, shuttleSeg, shuttleT, sSeg, sT
                                    );

                                    if (dAlong >= 0 && dAlong < bestDist && dAlong > 5.0) {
                                        bestDist = dAlong;
                                        bestStop = s;
                                    }
                                }

                                if (bestStop == null) {

                                    double minAbs = Double.POSITIVE_INFINITY;
                                    RouteStop minStop = null;

                                    for (RouteStop s : ordered) {
                                        var sn = polyService.nearestOnPolyline(
                                                usedPoly,
                                                s.getLatitude().doubleValue(),
                                                s.getLongitude().doubleValue()
                                        );
                                        int sSeg = sn.segmentIndex();
                                        double sT = sn.proj().t;

                                        double dAlong = polyService.distanceAlong(
                                                usedPoly, shuttleSeg, shuttleT, sSeg, sT
                                        );

                                        double absd = Math.abs(dAlong);
                                        if (absd < minAbs) {
                                            minAbs = absd;
                                            minStop = s;
                                        }
                                    }

                                    if (minStop != null) nextStopName = minStop.getStopName();

                                } else {
                                    nextStopName = bestStop.getStopName();
                                }

                            } else {
                                RouteStop nearest = stops.stream()
                                        .min(Comparator.comparingDouble(s ->
                                                com.shuttlebackend.utils.GeoUtils.haversineMeters(
                                                        latest.getLatitude().doubleValue(),
                                                        latest.getLongitude().doubleValue(),
                                                        s.getLatitude().doubleValue(),
                                                        s.getLongitude().doubleValue()
                                                )))
                                        .orElse(null);

                                if (nearest != null) nextStopName = nearest.getStopName();
                            }
                        }
                    }

                    ShuttleRealtimeDto realtime = new ShuttleRealtimeDto(
                            shuttleId,
                            latest.getLatitude().doubleValue(),
                            latest.getLongitude().doubleValue(),
                            direction,
                            speedKph,
                            nextStopName,
                            latest.getCreatedAt()
                    );

                } catch (Exception ex) {
                    LoggerFactory.getLogger(LocationBufferService.class).warn("Realtime processing error: ", ex);
                }

            });
        });
    }
}
