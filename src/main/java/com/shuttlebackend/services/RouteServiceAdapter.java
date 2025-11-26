package com.shuttlebackend.services;

import com.shuttlebackend.eta.model.Waypoint;
import com.shuttlebackend.eta.model.Telemetry;
import com.shuttlebackend.eta.repository.TelemetryRepository;
import com.shuttlebackend.eta.service.RouteService;
import com.shuttlebackend.entities.DriverSession;
import com.shuttlebackend.entities.RouteStop;
import com.shuttlebackend.entities.Shuttle;
import com.shuttlebackend.repositories.DriverSessionRepository;
import com.shuttlebackend.repositories.RouteStopRepository;
import com.shuttlebackend.repositories.ShuttleRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RouteServiceAdapter implements RouteService {

    private final DriverSessionRepository driverSessionRepository;
    private final RouteStopRepository routeStopRepository;
    private final ShuttleRepository shuttleRepository;
    private final TelemetryRepository telemetryRepository;

    public RouteServiceAdapter(DriverSessionRepository driverSessionRepository,
                               RouteStopRepository routeStopRepository,
                               ShuttleRepository shuttleRepository,
                               TelemetryRepository telemetryRepository) {
        this.driverSessionRepository = driverSessionRepository;
        this.routeStopRepository = routeStopRepository;
        this.shuttleRepository = shuttleRepository;
        this.telemetryRepository = telemetryRepository;
    }

    @Override
    public List<Waypoint> getRemainingWaypoints(String vehicleId) {
        Integer shuttleId = parseShuttleId(vehicleId);
        if (shuttleId == null) return List.of();

        Optional<DriverSession> activeOpt = driverSessionRepository.findActiveByShuttleId(shuttleId);
        if (activeOpt.isEmpty()) return List.of();

        DriverSession session = activeOpt.get();
        Integer routeId = session.getRoute() != null ? session.getRoute().getId() : null;
        if (routeId == null) return List.of();

        List<RouteStop> stops = routeStopRepository.findByRoute_IdOrderByStopOrderAsc(routeId);
        if (stops.isEmpty()) return List.of();

        // try to fetch last telemetry for shuttle (try shuttle id string, then license plate)
        Optional<Telemetry> lastTelem = telemetryRepository.findTopByVehicleIdOrderByRecordedAtDesc(String.valueOf(shuttleId));
        if (lastTelem.isEmpty()) {
            // try license plate
            Optional<Shuttle> s = shuttleRepository.findById(shuttleId);
            if (s.isPresent()) {
                try {
                    String plate = s.get().getLicensePlate();
                    lastTelem = telemetryRepository.findTopByVehicleIdOrderByRecordedAtDesc(plate);
                } catch (Exception ignored) {}
            }
        }

        List<Waypoint> all = stops.stream()
                .map(rs -> new Waypoint(rs.getLatitude().doubleValue(), rs.getLongitude().doubleValue()))
                .collect(Collectors.toList());

        if (lastTelem.isEmpty()) return all; // no telemetry -> return full route

        // find nearest stop index to last telemetry
        double tlat = lastTelem.get().getLatitude();
        double tlon = lastTelem.get().getLongitude();

        int bestIndex = 0;
        double bestDist = Double.MAX_VALUE;
        for (int i = 0; i < all.size(); i++) {
            Waypoint wp = all.get(i);
            double d = Haversine.distanceMeters(tlat, tlon, wp.getLat(), wp.getLon());
            if (d < bestDist) {
                bestDist = d;
                bestIndex = i;
            }
        }

        // return from nearest stop onward
        if (bestIndex < 0 || bestIndex >= all.size()) return all;
        return new ArrayList<>(all.subList(bestIndex, all.size()));
    }

    @Override
    public Optional<Instant> getNextScheduledArrival(String vehicleId) {
        return Optional.empty();
    }

    private Integer parseShuttleId(String vehicleId) {
        if (vehicleId == null) return null;
        try {
            return Integer.valueOf(vehicleId);
        } catch (NumberFormatException ex) {
            Optional<Shuttle> s = shuttleRepository.findByLicensePlate(vehicleId);
            return s.map(Shuttle::getId).orElse(null);
        }
    }

    // small internal haversine helper to avoid cross-package util dependency
    private static class Haversine {
        private static final double R = 6371000.0;
        static double distanceMeters(double lat1, double lon1, double lat2, double lon2) {
            double dLat = Math.toRadians(lat2 - lat1);
            double dLon = Math.toRadians(lon2 - lon1);
            double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon/2) * Math.sin(dLon/2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
            return R * c;
        }
    }
}
