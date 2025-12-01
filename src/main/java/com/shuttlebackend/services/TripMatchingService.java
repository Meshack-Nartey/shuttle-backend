package com.shuttlebackend.services;

import com.shuttlebackend.dtos.MatchedRouteDto;
import com.shuttlebackend.dtos.MatchedShuttleDto;
import com.shuttlebackend.entities.DriverSession;
import com.shuttlebackend.entities.LocationUpdate;
import com.shuttlebackend.entities.Shuttle;
import com.shuttlebackend.entities.Route;
import com.shuttlebackend.repositories.DriverSessionRepository;
import com.shuttlebackend.repositories.LocationUpdateRepository;
import com.shuttlebackend.repositories.RouteRepository;
import com.shuttlebackend.repositories.ShuttleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TripMatchingService {

    private final RouteRepository routeRepository;
    private final DriverSessionRepository driverSessionRepository;
    private final LocationUpdateRepository locationUpdateRepository;
    private final ShuttleRepository shuttleRepository;

    public List<MatchedRouteDto> matchTrip(Integer pickupStopId, Integer dropoffStopId) {
        List<Route> routes = routeRepository.findRoutesContainingBothStops(pickupStopId, dropoffStopId);

        List<MatchedRouteDto> result = new ArrayList<>();

        for (Route route : routes) {
            // find active driver sessions for this route
            List<DriverSession> activeSessions = driverSessionRepository.findByRoute_IdAndEndedAtIsNull(route.getId());

            List<MatchedShuttleDto> matchedShuttles = activeSessions.stream().map(session -> {
                Shuttle shuttle = session.getShuttle();
                Integer shuttleId = shuttle != null ? shuttle.getId() : null;

                String shuttleExternalId = shuttle != null ? shuttle.getExternalId() : null;
                String status = shuttle != null ? shuttle.getStatus() : null;

                Double lat = null;
                Double lon = null;
                if (shuttleId != null) {
                    Optional<LocationUpdate> locOpt = locationUpdateRepository.findTop1ByShuttle_IdOrderByCreatedAtDesc(shuttleId);
                    if (locOpt.isPresent()) {
                        LocationUpdate lu = locOpt.get();
                        lat = lu.getLatitude() != null ? lu.getLatitude().doubleValue() : null;
                        lon = lu.getLongitude() != null ? lu.getLongitude().doubleValue() : null;
                    }
                }

                // ETA calculation is out of scope; set null
                Long eta = null;

                return new MatchedShuttleDto(shuttleId, shuttleExternalId, status, lat, lon, eta);
            }).collect(Collectors.toList());

            MatchedRouteDto mr = new MatchedRouteDto(route.getId(), route.getRouteName(), matchedShuttles);
            result.add(mr);
        }

        return result;
    }
}
