package com.shuttlebackend.eta.service;

import com.shuttlebackend.eta.dto.VehicleEtaDto;
import com.shuttlebackend.eta.model.Telemetry;
import com.shuttlebackend.eta.model.Waypoint;
import com.shuttlebackend.eta.repository.TelemetryRepository;
import com.shuttlebackend.eta.util.HaversineUtil;
import com.shuttlebackend.eta.service.RouteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EtaService {
    private static final Logger logger = LoggerFactory.getLogger(EtaService.class);
    private static final int MIN_TELEMETRY_FOR_AVG = 2;

    private final TelemetryRepository telemetryRepository;
    private final RouteService routeService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ConcurrentHashMap<String, Object> locks = new ConcurrentHashMap<>();

    public EtaService(TelemetryRepository telemetryRepository,
                      RouteService routeService,
                      SimpMessagingTemplate messagingTemplate) {
        this.telemetryRepository = telemetryRepository;
        this.routeService = routeService;
        this.messagingTemplate = messagingTemplate;
    }

    public VehicleEtaDto computeAndPublishEta(String vehicleId) {
        if (vehicleId == null || vehicleId.isBlank()) throw new IllegalArgumentException("vehicleId required");
        Object lock = locks.computeIfAbsent(vehicleId, k -> new Object());
        synchronized (lock) {
            VehicleEtaDto dto = computeEta(vehicleId);
            try { messagingTemplate.convertAndSend("/topic/vehicles/" + vehicleId + "/eta", dto); } catch (Exception ex) { logger.warn("publish failed: {}", ex.getMessage()); }
            return dto;
        }
    }

    public VehicleEtaDto computeEta(String vehicleId) {
        Optional<Telemetry> lastOpt = telemetryRepository.findTopByVehicleIdOrderByRecordedAtDesc(vehicleId);
        List<Waypoint> remaining = routeService.getRemainingWaypoints(vehicleId);
        Instant now = Instant.now();

        if (lastOpt.isPresent() && !CollectionUtils.isEmpty(remaining)) {
            Telemetry last = lastOpt.get();
            double distanceMeters = HaversineUtil.cumulativeDistanceMeters(last.getLatitude(), last.getLongitude(), remaining);

            double speed = estimateSpeed(last, vehicleId);
            if (speed > 0.01) {
                long travelMillis = (long) ((distanceMeters / speed) * 1000.0);
                Instant eta = now.plusMillis(travelMillis);
                return new VehicleEtaDto(vehicleId, eta, eta.toEpochMilli(), distanceMeters, "telemetry");
            }
        }
        return new VehicleEtaDto(vehicleId, null, 0L, 0.0, "unavailable");
    }

    private double estimateSpeed(Telemetry last, String vehicleId) {
        if (last.getSpeedMetersPerSecond() > 0.01) return Math.max(0.0, last.getSpeedMetersPerSecond());
        List<Telemetry> recent = telemetryRepository.findTop5ByVehicleIdOrderByRecordedAtDesc(vehicleId);
        if (recent == null || recent.size() < MIN_TELEMETRY_FOR_AVG) return 0.0;
        double sum = 0.0; int cnt = 0;
        for (Telemetry t : recent) { if (t.getSpeedMetersPerSecond() > 0.01) { sum += t.getSpeedMetersPerSecond(); cnt++; } }
        return cnt > 0 ? sum / cnt : 0.0;
    }
}
