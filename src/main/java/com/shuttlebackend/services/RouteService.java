package com.shuttlebackend.services;

import com.shuttlebackend.dtos.RouteResponseDto;
import com.shuttlebackend.dtos.StopDto;
import com.shuttlebackend.entities.Route;
import com.shuttlebackend.entities.RouteStop;
import com.shuttlebackend.repositories.RouteRepository;
import com.shuttlebackend.repositories.DriverSessionRepository;
import com.shuttlebackend.repositories.ShuttleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RouteService {

    private final RouteRepository routeRepository;
    private final DriverSessionRepository driverSessionRepository;
    private final ShuttleRepository shuttleRepository;

    // Given a shuttleId, find active session -> route -> polyline and stops
    public Optional<RouteResponseDto> getPointsForShuttle(Long shuttleId) {
        if (shuttleId == null) return Optional.empty();

        Optional<com.shuttlebackend.entities.DriverSession> sessionOpt = driverSessionRepository.findActiveByShuttleId(shuttleId.intValue());
        if (sessionOpt.isEmpty()) return Optional.empty();

        var session = sessionOpt.get();
        var route = session.getRoute();
        if (route == null) return Optional.empty();

        // polyline stored as List<List<Double>> in entity, currently [lat, lon] per existing comment — normalize to [lng, lat]
        List<List<Double>> polyline = new ArrayList<>();
        if (route.getPolylineForward() != null) {
            for (List<Double> p : route.getPolylineForward()) {
                if (p.size() >= 2) {
                    // entity uses [lat, lon] — convert to [lng, lat]
                    double lat = p.get(0);
                    double lon = p.get(1);
                    polyline.add(List.of(lon, lat));
                }
            }
        }

        List<StopDto> stops = new ArrayList<>();
        List<RouteStop> routeStops = route.getRouteStops();
        if (routeStops != null) {
            stops = routeStops.stream()
                    .map(rs -> new StopDto(rs.getStopOrder(), rs.getStopName(), rs.getLatitude().doubleValue(), rs.getLongitude().doubleValue()))
                    .collect(Collectors.toList());
        }

        RouteResponseDto resp = new RouteResponseDto(route.getId(), route.getRouteName(), polyline, stops);
        return Optional.of(resp);
    }

    public Optional<Route> findById(Integer id) {
        return routeRepository.findById(id);
    }

    public List<Route> findBySchool(Integer schoolId) {
        return routeRepository.findBySchool_Id(schoolId);
    }
}
