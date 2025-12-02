package com.shuttlebackend.utils;

import java.util.List;

public class GeoUtils {
    private static final double EARTH_RADIUS_M = 6371000d;

    public static double haversineMeters(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double rLat1 = Math.toRadians(lat1);
        double rLat2 = Math.toRadians(lat2);

        double a = Math.sin(dLat/2) * Math.sin(dLat/2)
                 + Math.cos(rLat1) * Math.cos(rLat2)
                 * Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return EARTH_RADIUS_M * c;
    }

    // Project point p onto segment ab; returns fraction t in [0..1] and projected lat/lon
    public static class Projection {
        public final double lat;
        public final double lon;
        public final double t;
        public Projection(double lat, double lon, double t) { this.lat = lat; this.lon = lon; this.t = t; }
    }

    public static Projection projectOnSegment(double pLat, double pLon,
                                              double aLat, double aLon,
                                              double bLat, double bLon) {
        // treat lon as x, lat as y (reasonable for small segments)
        double x1 = aLon, y1 = aLat;
        double x2 = bLon, y2 = bLat;
        double px = pLon, py = pLat;
        double vx = x2 - x1;
        double vy = y2 - y1;
        double wx = px - x1;
        double wy = py - y1;
        double denom = vx*vx + vy*vy;
        double t = denom == 0 ? 0 : (wx*vx + wy*vy) / denom;
        if (Double.isNaN(t)) t = 0;
        t = Math.max(0, Math.min(1, t));
        double projX = x1 + t * vx;
        double projY = y1 + t * vy;
        return new Projection(projY, projX, t);
    }

    public static class Nearest {
        public final int segmentIndex; // index of segment start
        public final Projection proj;
        public Nearest(int segmentIndex, Projection proj) { this.segmentIndex = segmentIndex; this.proj = proj; }
    }

    // polyline: list of [lat, lon]
    public static Nearest nearestPointOnPolyline(double lat, double lon, List<double[]> polyline) {
        if (polyline == null || polyline.isEmpty()) return new Nearest(0, new Projection(lat, lon, 0));
        double bestDist = Double.POSITIVE_INFINITY;
        int bestIdx = 0;
        Projection bestProj = new Projection(polyline.get(0)[0], polyline.get(0)[1], 0);

        if (polyline.size() == 1) {
            double[] p = polyline.get(0);
            bestProj = new Projection(p[0], p[1], 0);
            return new Nearest(0, bestProj);
        }

        for (int i = 0; i < polyline.size() - 1; i++) {
            double[] a = polyline.get(i);
            double[] b = polyline.get(i+1);
            Projection proj = projectOnSegment(lat, lon, a[0], a[1], b[0], b[1]);
            double d = haversineMeters(lat, lon, proj.lat, proj.lon);
            if (d < bestDist) {
                bestDist = d;
                bestIdx = i;
                bestProj = proj;
            }
        }
        return new Nearest(bestIdx, bestProj);
    }

    public static double[] cumulativeDistances(List<double[]> polyline) {
        int n = polyline == null ? 0 : polyline.size();
        double[] cum = new double[Math.max(1, n)];
        if (n == 0) return cum;
        cum[0] = 0;
        for (int i = 1; i < n; i++) {
            double[] prev = polyline.get(i-1);
            double[] cur = polyline.get(i);
            cum[i] = cum[i-1] + haversineMeters(prev[0], prev[1], cur[0], cur[1]);
        }
        return cum;
    }

    // distance from projection on segment (segFrom,tFrom) forward to projection (segTo,tTo)
    // handles wrap by summing to end then from start to segTo if segFrom > segTo
    public static double distanceAlongPolyline(List<double[]> polyline, int segFrom, double tFrom, int segTo, double tTo) {
        if (polyline == null || polyline.isEmpty()) return 0d;
        if (segFrom < 0) segFrom = 0;
        if (segTo < 0) segTo = 0;
        int n = polyline.size();
        double[] cum = cumulativeDistances(polyline);

        if (segFrom >= n) segFrom = n-1;
        if (segTo >= n) segTo = n-1;

        // normalize: if same segment
        if (segFrom == segTo) {
            double[] a = polyline.get(segFrom);
            double[] b = polyline.get(Math.min(segFrom+1, n-1));
            double segLen = haversineMeters(a[0], a[1], b[0], b[1]);
            return Math.abs(tTo - tFrom) * segLen;
        }

        // helper to compute forward distance from segA (tA) to segB (tB) assuming segA < segB
        java.util.function.BiFunction<Integer,Integer,Double> forwardDist = (a, b) -> {
            // distance from projected point to end of its segment
            double fromSegLen = haversineMeters(polyline.get(a)[0], polyline.get(a)[1], polyline.get(Math.min(a+1, n-1))[0], polyline.get(Math.min(a+1, n-1))[1]);
            double distFrom = (1 - tFrom) * fromSegLen;

            // distance from start of target segment to projected point
            double toSegLen = haversineMeters(polyline.get(b)[0], polyline.get(b)[1], polyline.get(Math.min(b+1, n-1))[0], polyline.get(Math.min(b+1, n-1))[1]);
            double distTo = tTo * toSegLen;

            double middle = 0d;
            int startFull = a + 1;
            int endFull = b - 1;
            if (endFull >= startFull) {
                middle = cum[endFull+1] - cum[startFull];
            }

            return distFrom + middle + distTo;
        };

        if (segFrom < segTo) {
            return forwardDist.apply(segFrom, segTo);
        }

        // segFrom > segTo: wrap around (sum segFrom->end) + (start->segTo)
        double distToEnd = forwardDist.apply(segFrom, n-1);
        // compute from start (segment 0) to segTo
        // for the start projection, tFromStart is 0
        double distFromStartTo = distanceAlongPolyline(polyline, 0, 0.0, segTo, tTo);
        return distToEnd + distFromStartTo;
    }
}
