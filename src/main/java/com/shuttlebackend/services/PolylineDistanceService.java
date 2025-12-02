package com.shuttlebackend.services;

import com.shuttlebackend.utils.GeoUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class PolylineDistanceService {

    // cache routeId -> parsed polyline (list of [lat, lon])
    private final Map<Integer, List<double[]>> cache = new ConcurrentHashMap<>();

    public List<double[]> fromTypedPolyline(List<List<Double>> typed) {
        if (typed == null || typed.isEmpty()) return List.of();
        return typed.stream().map(p -> new double[]{p.get(0), p.get(1)}).toList();
    }

    public List<double[]> getPolylineForRoute(Integer routeId, List<List<Double>> typedPolyline) {
        if (routeId == null) return fromTypedPolyline(typedPolyline);
        return cache.computeIfAbsent(routeId, id -> fromTypedPolyline(typedPolyline));
    }

    public GeoUtils.Nearest nearestOnPolyline(Integer routeId, List<List<Double>> typedPolyline, double lat, double lon) {
        List<double[]> poly = getPolylineForRoute(routeId, typedPolyline);
        return GeoUtils.nearestPointOnPolyline(lat, lon, poly);
    }

    // overload: accept just typed polyline
    public GeoUtils.Nearest nearestOnPolyline(List<List<Double>> typedPolyline, double lat, double lon) {
        List<double[]> poly = fromTypedPolyline(typedPolyline);
        return GeoUtils.nearestPointOnPolyline(lat, lon, poly);
    }

    public double distanceAlong(Integer routeId, List<List<Double>> typedPolyline, int segFrom, double tFrom, int segTo, double tTo) {
        List<double[]> poly = getPolylineForRoute(routeId, typedPolyline);
        return GeoUtils.distanceAlongPolyline(poly, segFrom, tFrom, segTo, tTo);
    }

    // overload: accept just typed polyline
    public double distanceAlong(List<List<Double>> typedPolyline, int segFrom, double tFrom, int segTo, double tTo) {
        List<double[]> poly = fromTypedPolyline(typedPolyline);
        return GeoUtils.distanceAlongPolyline(poly, segFrom, tFrom, segTo, tTo);
    }
}
