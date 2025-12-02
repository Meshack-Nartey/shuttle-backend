package com.shuttlebackend.services;

import com.shuttlebackend.utils.GeoUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectionDetectorService {

    public enum Direction { FORWARD, BACKWARD, UNKNOWN }

    private final PolylineDistanceService polyService;

    // threshold in meters under which distances are considered equal -> unknown
    private static final double DISTANCE_THRESHOLD = 10.0;

    public Direction detectDirection(Integer shuttleId, Integer routeId, double lat, double lon, List<List<Double>> polyForward, List<List<Double>> polyBackward) {
        double distF = Double.POSITIVE_INFINITY;
        double distB = Double.POSITIVE_INFINITY;

        if (polyForward != null && !polyForward.isEmpty()) {
            var nf = polyService.nearestOnPolyline(routeId, polyForward, lat, lon);
            distF = GeoUtils.haversineMeters(lat, lon, nf.proj.lat, nf.proj.lon);
        }

        if (polyBackward != null && !polyBackward.isEmpty()) {
            var nb = polyService.nearestOnPolyline(routeId, polyBackward, lat, lon);
            distB = GeoUtils.haversineMeters(lat, lon, nb.proj.lat, nb.proj.lon);
        }

        if (Double.isInfinite(distF) && Double.isInfinite(distB)) return Direction.UNKNOWN;

        if (Math.abs(distF - distB) <= DISTANCE_THRESHOLD) return Direction.UNKNOWN;
        return distF < distB ? Direction.FORWARD : Direction.BACKWARD;
    }
}
