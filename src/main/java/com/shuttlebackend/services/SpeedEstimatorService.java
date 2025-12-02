package com.shuttlebackend.services;

import com.shuttlebackend.entities.LocationUpdate;
import com.shuttlebackend.repositories.LocationUpdateRepository;
import com.shuttlebackend.utils.GeoUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
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

        // compute speed over whole window (latest to oldest)
        LocationUpdate latest = recent.get(0);
        LocationUpdate oldest = recent.get(recent.size() - 1);

        double meters = GeoUtils.haversineMeters(
                latest.getLatitude().doubleValue(), latest.getLongitude().doubleValue(),
                oldest.getLatitude().doubleValue(), oldest.getLongitude().doubleValue()
        );
        long seconds = Math.max(1, Duration.between(oldest.getCreatedAt(), latest.getCreatedAt()).getSeconds());
        double mps = meters / seconds;
        double kph = (mps * 3600.0) / 1000.0;
        if (Double.isNaN(kph) || kph <= 0) return FALLBACK_KPH;
        return kph;
    }
}

