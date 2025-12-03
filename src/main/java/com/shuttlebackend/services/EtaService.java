package com.shuttlebackend.services;

import com.shuttlebackend.dtos.EtaResponseDto;
import com.shuttlebackend.entities.DriverSession;
import com.shuttlebackend.entities.LocationUpdate;
import com.shuttlebackend.entities.Route;
import com.shuttlebackend.entities.RouteStop;
import com.shuttlebackend.repositories.DriverSessionRepository;
import com.shuttlebackend.repositories.LocationUpdateRepository;
import com.shuttlebackend.repositories.RouteStopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EtaService {

    private final DriverSessionRepository sessionRepository;
    private final LocationUpdateRepository locationUpdateRepository;
    private final RouteStopRepository routeStopRepository;
    private final PolylineDistanceService polyService;
    private final SpeedEstimatorService speedEstimator;
    private final DirectionDetectorService directionDetector;
    private final SimpMessagingTemplate messagingTemplate;

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_INSTANT.withZone(ZoneOffset.UTC);

    public EtaResponseDto calculateEta(Integer shuttleId, Integer pickupStopId, Integer dropoffStopId) {
        if (shuttleId == null || pickupStopId == null || dropoffStopId == null) {
            throw new IllegalArgumentException("shuttleId, pickupStopId and dropoffStopId are required");
        }

        Optional<DriverSession> sessOpt = sessionRepository.findActiveByShuttleId(shuttleId);
        if (sessOpt.isEmpty()) {
            EtaResponseDto resp = new EtaResponseDto(shuttleId, pickupStopId, dropoffStopId, 0L, ISO.format(Instant.now()), 0.0, 0.0, 0.0, "unknown", 0.0, 0,0,0, "no_active_session");
            publishEta(shuttleId, resp);
            return resp;
        }
        DriverSession sess = sessOpt.get();
        Route route = sess.getRoute();
        if (route == null) {
            EtaResponseDto resp = new EtaResponseDto(shuttleId, pickupStopId, dropoffStopId, 0L, ISO.format(Instant.now()), 0.0, 0.0, 0.0, "unknown", 0.0, 0,0,0, "no_route_on_session");
            publishEta(shuttleId, resp);
            return resp;
        }

        // latest location
        Optional<LocationUpdate> locOpt = locationUpdateRepository.findTop1ByShuttle_IdOrderByCreatedAtDesc(shuttleId);
        if (locOpt.isEmpty()) {
            EtaResponseDto resp = new EtaResponseDto(shuttleId, pickupStopId, dropoffStopId, 0L, ISO.format(Instant.now()), 0.0, 0.0, 0.0, "unknown", 0.0, 0,0,0, "no_location");
            publishEta(shuttleId, resp);
            return resp;
        }
        LocationUpdate loc = locOpt.get();
        double lat = loc.getLatitude().doubleValue();
        double lon = loc.getLongitude().doubleValue();

        // determine direction by nearest polyline
        List<List<Double>> polyF = route.getPolylineForward();
        List<List<Double>> polyB = route.getPolylineBackward();
        DirectionDetectorService.Direction dir = directionDetector.detectDirection(shuttleId, route.getId(), lat, lon, polyF, polyB);
        String dirStr = dir == null ? "unknown" : dir.name().toLowerCase();

        // pick the correct polyline
        List<List<Double>> usedPoly = null;
        if (dir == DirectionDetectorService.Direction.FORWARD) usedPoly = polyF;
        else if (dir == DirectionDetectorService.Direction.BACKWARD) usedPoly = polyB;

        if (usedPoly == null || usedPoly.isEmpty()) {
            EtaResponseDto resp = new EtaResponseDto(shuttleId, pickupStopId, dropoffStopId, 0L, ISO.format(Instant.now()), 0.0, 0.0, 0.0, dirStr, 0.0, 0,0,0, "no_polyline_for_direction");
            publishEta(shuttleId, resp);
            return resp;
        }

        // retrieve stops
        RouteStop pickup = routeStopRepository.findById(pickupStopId).orElse(null);
        RouteStop dropoff = routeStopRepository.findById(dropoffStopId).orElse(null);
        if (pickup == null || dropoff == null) {
            EtaResponseDto resp = new EtaResponseDto(shuttleId, pickupStopId, dropoffStopId, 0L, ISO.format(Instant.now()), 0.0, 0.0, 0.0, dirStr, 0.0, 0,0,0, "missing_stop");
            publishEta(shuttleId, resp);
            return resp;
        }

        // NEW: validate that both stops exist in the chosen direction and that pickup is before dropoff
        try {
            List<RouteStop> orderedStops = routeStopRepository.findByRoute_IdOrderByStopOrderAsc(route.getId());
            // find indices in forward order
            int pickIdx = -1, dropIdx = -1;
            for (int i = 0; i < orderedStops.size(); i++) {
                RouteStop rs = orderedStops.get(i);
                if (rs.getId().equals(pickup.getId())) pickIdx = i;
                if (rs.getId().equals(dropoff.getId())) dropIdx = i;
            }
            if (dir == DirectionDetectorService.Direction.FORWARD) {
                if (pickIdx == -1 || dropIdx == -1) {
                    EtaResponseDto resp = new EtaResponseDto(shuttleId, pickupStopId, dropoffStopId, 0L, ISO.format(Instant.now()), 0.0, 0.0, 0.0, dirStr, 0.0, 0,0,0, "stop_not_on_direction");
                    publishEta(shuttleId, resp);
                    return resp;
                }
                if (pickIdx >= dropIdx) {
                    EtaResponseDto resp = new EtaResponseDto(shuttleId, pickupStopId, dropoffStopId, 0L, ISO.format(Instant.now()), 0.0, 0.0, 0.0, dirStr, 0.0, 0,0,0, "invalid_stop_order");
                    publishEta(shuttleId, resp);
                    return resp;
                }
            } else if (dir == DirectionDetectorService.Direction.BACKWARD) {
                // backward direction corresponds to reverse of forward-ordered stops
                if (pickIdx == -1 || dropIdx == -1) {
                    EtaResponseDto resp = new EtaResponseDto(shuttleId, pickupStopId, dropoffStopId, 0L, ISO.format(Instant.now()), 0.0, 0.0, 0.0, dirStr, 0.0, 0,0,0, "stop_not_on_direction");
                    publishEta(shuttleId, resp);
                    return resp;
                }
                // in backward travel, pickup should appear after dropoff in forward ordering
                if (pickIdx <= dropIdx) {
                    EtaResponseDto resp = new EtaResponseDto(shuttleId, pickupStopId, dropoffStopId, 0L, ISO.format(Instant.now()), 0.0, 0.0, 0.0, dirStr, 0.0, 0,0,0, "invalid_stop_order");
                    publishEta(shuttleId, resp);
                    return resp;
                }
            }
        } catch (Exception ex) {
            // if repository lookup fails for any reason, fail gracefully
            EtaResponseDto resp = new EtaResponseDto(shuttleId, pickupStopId, dropoffStopId, 0L, ISO.format(Instant.now()), 0.0, 0.0, 0.0, dirStr, 0.0, 0,0,0, "stop_validation_error");
            publishEta(shuttleId, resp);
            return resp;
        }

        // Using coordinates: snap them to the used polyline and compute indices
        var shuttleNearest = polyService.nearestOnPolyline(usedPoly, lat, lon);
        int shuttleSeg = shuttleNearest.segmentIndex();
        double shuttleT = shuttleNearest.proj().t;

        var pickupNearest = polyService.nearestOnPolyline(usedPoly, pickup.getLatitude().doubleValue(), pickup.getLongitude().doubleValue());
        int pickSeg = pickupNearest.segmentIndex();
        double pickT = pickupNearest.proj().t;

        var dropNearest = polyService.nearestOnPolyline(usedPoly, dropoff.getLatitude().doubleValue(), dropoff.getLongitude().doubleValue());
        int dropSeg = dropNearest.segmentIndex();
        double dropT = dropNearest.proj().t;

        // compute distances
        double distShuttleToPickup = polyService.distanceAlong(usedPoly, shuttleSeg, shuttleT, pickSeg, pickT);
        double distPickupToDrop = polyService.distanceAlong(usedPoly, pickSeg, pickT, dropSeg, dropT);
        double total = distShuttleToPickup + distPickupToDrop;

        // estimate speed
        double speedKph = speedEstimator.estimateSpeedKph(shuttleId);
        double speedMps = speedKph * 1000.0 / 3600.0;
        long etaSeconds = speedMps <= 0 ? 0L : Math.round(total / speedMps);
        long etaMillis = etaSeconds * 1000L;
        String etaTs = ISO.format(Instant.now().plusSeconds(etaSeconds));

        EtaResponseDto resp = new EtaResponseDto(
                shuttleId,
                pickup.getId(),
                dropoff.getId(),
                etaMillis,
                etaTs,
                distShuttleToPickup,
                distPickupToDrop,
                total,
                dirStr,
                speedKph,
                shuttleSeg,
                pickSeg,
                dropSeg,
                "ok"
        );

        publishEta(shuttleId, resp);

        return resp;
    }

    private void publishEta(Integer shuttleId, EtaResponseDto resp) {
        try {
            String topic = "/topic/shuttle/" + shuttleId + "/eta";
            messagingTemplate.convertAndSend(topic, resp);
        } catch (Exception ex) {
            // logging omitted to keep code concise
        }
    }
}
