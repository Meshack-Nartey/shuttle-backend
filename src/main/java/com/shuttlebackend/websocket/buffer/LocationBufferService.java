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
    private final RouteStopRepository routeStopRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final DirectionDetectorService directionDetector;
    private final SpeedEstimatorService speedEstimator;
    private final PolylineDistanceService polyService;

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

                            // Enriched realtime DTO: direction, speed, nextStop
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

                                // find next stop by nearest stop order + direction
                                List<RouteStop> stops = routeStopRepository.findByRoute_IdOrderByStopOrderAsc(route.getId());
                                if (stops != null && !stops.isEmpty()) {
                                    // pick polyline according to direction
                                    List<List<Double>> usedPoly = (dirEnum == null || dirEnum == com.shuttlebackend.services.DirectionDetectorService.Direction.UNKNOWN)
                                            ? route.getPolylineForward()
                                            : (dirEnum == com.shuttlebackend.services.DirectionDetectorService.Direction.FORWARD
                                            ? route.getPolylineForward() : route.getPolylineBackward());

                                    if (usedPoly != null && !usedPoly.isEmpty()) {
                                        // ensure correct ordering of stops for direction (forward = asc, backward = desc)
                                        List<RouteStop> ordered = new java.util.ArrayList<>(stops);
                                        if (dirEnum == com.shuttlebackend.services.DirectionDetectorService.Direction.BACKWARD) {
                                            java.util.Collections.reverse(ordered);
                                        }

                                        // snap shuttle to polyline
                                        var shuttleNearest2 = polyService.nearestOnPolyline(usedPoly, latest.getLatitude().doubleValue(), latest.getLongitude().doubleValue());
                                        int shuttleSeg2 = shuttleNearest2.segmentIndex;
                                        double shuttleT2 = shuttleNearest2.proj.t;

                                        double bestDist = Double.POSITIVE_INFINITY;
                                        RouteStop bestStop = null;

                                        // find next stop with minimal positive distance along polyline
                                        for (RouteStop s : ordered) {
                                            var sn = polyService.nearestOnPolyline(usedPoly, s.getLatitude().doubleValue(), s.getLongitude().doubleValue());
                                            int sSeg = sn.segmentIndex;
                                            double sT = sn.proj.t;
                                            double dAlong = polyService.distanceAlong(usedPoly, shuttleSeg2, shuttleT2, sSeg, sT);
                                            if (dAlong >= 0 && dAlong < bestDist && dAlong > 5.0) { // prefer stops at least 5m ahead
                                                bestDist = dAlong;
                                                bestStop = s;
                                            }
                                        }

                                        if (bestStop == null) {
                                            // fallback: pick nearest by absolute polyline distance (could be behind)
                                            double minAbs = Double.POSITIVE_INFINITY;
                                            RouteStop minStop = null;
                                            for (RouteStop s : ordered) {
                                                var sn = polyService.nearestOnPolyline(usedPoly, s.getLatitude().doubleValue(), s.getLongitude().doubleValue());
                                                int sSeg = sn.segmentIndex;
                                                double sT = sn.proj.t;
                                                double dAlong = polyService.distanceAlong(usedPoly, shuttleSeg2, shuttleT2, sSeg, sT);
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
                                        // no polyline available - fallback to straight-line nearest stop
                                        RouteStop nearest = stops.stream().min(java.util.Comparator.comparingDouble(s ->
                                                com.shuttlebackend.utils.GeoUtils.haversineMeters(latest.getLatitude().doubleValue(), latest.getLongitude().doubleValue(), s.getLatitude().doubleValue(), s.getLongitude().doubleValue())
                                        )).orElse(null);
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

                            // publish enriched realtime topic for students (same as earlier topic)
                            messagingTemplate.convertAndSend(topic, realtime);
                        }
                    } catch (Exception ex) {
                        // don't fail flush if student publish fails
                    }
                });
            });
        }
    }
}
