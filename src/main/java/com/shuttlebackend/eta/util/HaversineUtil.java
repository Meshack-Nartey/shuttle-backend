package com.shuttlebackend.eta.util;

import com.shuttlebackend.eta.model.Waypoint;

import java.util.List;

public final class HaversineUtil {
    private static final double EARTH_RADIUS_METERS = 6371000.0;

    private HaversineUtil() {}

    public static double distanceMeters(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return EARTH_RADIUS_METERS * c;
    }

    public static double cumulativeDistanceMeters(double startLat, double startLon, List<Waypoint> waypoints) {
        if (waypoints == null || waypoints.isEmpty()) return 0.0;
        double total = 0.0;
        Waypoint prev = waypoints.get(0);
        total += distanceMeters(startLat, startLon, prev.getLat(), prev.getLon());
        for (int i = 1; i < waypoints.size(); i++) {
            Waypoint cur = waypoints.get(i);
            total += distanceMeters(prev.getLat(), prev.getLon(), cur.getLat(), cur.getLon());
            prev = cur;
        }
        return total;
    }
}

