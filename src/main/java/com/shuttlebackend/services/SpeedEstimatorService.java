package com.shuttlebackend.services;

import com.shuttlebackend.entities.LocationUpdate;
import com.shuttlebackend.repositories.LocationUpdateRepository;
import com.shuttlebackend.utils.GeoUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SpeedEstimatorService {
    private final LocationUpdateRepository locationRepo;

    // fallback speed when no movement (kph)
    private static final double FALLBACK_KPH = 10.0;

    public double estimateSpeedKph(Integer shuttleId) {
        if (shuttleId == null) return FALLBACK_KPH;
        List<LocationUpdate> recent = locationRepo.findTop20ByShuttle_IdOrderByCreatedAtDesc(shuttleId);
        if (recent == null || recent.size() < 2) return FALLBACK_KPH;

        // Use up to the last N updates (latest first)
        final int N = Math.min(5, recent.size());
        List<LocationUpdate> window = recent.subList(0, N);
        if (window.size() < 2) return FALLBACK_KPH;

        // compute segment speeds (m/s) between consecutive updates and average them
        List<Double> segmentMps = new ArrayList<>();
        for (int i = 0; i < window.size() - 1; i++) {
            LocationUpdate cur = window.get(i);       // newer
            LocationUpdate next = window.get(i + 1);  // older
            double meters = GeoUtils.haversineMeters(
                    cur.getLatitude().doubleValue(), cur.getLongitude().doubleValue(),
                    next.getLatitude().doubleValue(), next.getLongitude().doubleValue()
            );
            long seconds = Math.max(0, Duration.between(next.getCreatedAt(), cur.getCreatedAt()).getSeconds());
            if (seconds <= 0) continue; // skip zero/negative duration
            double mps = meters / (double) seconds;
            if (Double.isFinite(mps) && mps > 0) segmentMps.add(mps);
        }

        if (segmentMps.isEmpty()) return FALLBACK_KPH;

        double sum = 0.0;
        for (Double v : segmentMps) sum += v;
        double avgMps = sum / segmentMps.size();
        double kph = (avgMps * 3600.0) / 1000.0;
        if (Double.isNaN(kph) || kph <= 0) return FALLBACK_KPH;
        return kph;
    }
}
