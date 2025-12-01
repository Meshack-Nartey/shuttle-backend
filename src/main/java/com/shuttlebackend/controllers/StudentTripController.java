package com.shuttlebackend.controllers;

import com.shuttlebackend.dtos.TripMatchRequest;
import com.shuttlebackend.dtos.MatchedRouteDto;
import com.shuttlebackend.entities.RouteStop;
import com.shuttlebackend.repositories.RouteStopRepository;
import com.shuttlebackend.services.TripMatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/student/trip")
@RequiredArgsConstructor
public class StudentTripController {

    private final TripMatchingService tripMatchingService;
    private final RouteStopRepository routeStopRepository;

    @PostMapping("/match")
    public ResponseEntity<?> matchTrip(@RequestBody TripMatchRequest req) {
        if (req == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "request body is required"));
        }

        Integer pickupId = req.getPickupStopId();
        Integer dropoffId = req.getDropoffStopId();

        // Resolve names if IDs not provided
        if (pickupId == null) {
            String pname = req.getPickupStopName();
            if (pname != null && !pname.isBlank()) {
                List<RouteStop> found = routeStopRepository.findByStopNameContainingIgnoreCase(pname);
                if (found.isEmpty()) {
                    return ResponseEntity.badRequest().body(Map.of("error", "pickup stop not found: " + pname));
                }
                // try exact (case-insensitive) match
                List<RouteStop> exact = found.stream()
                        .filter(r -> r.getStopName().equalsIgnoreCase(pname))
                        .collect(Collectors.toList());
                if (exact.size() == 1) pickupId = exact.get(0).getId();
                else if (found.size() == 1) pickupId = found.get(0).getId();
                else return ResponseEntity.badRequest().body(Map.of("error", "pickup stop name is ambiguous; provide the stop id", "matches", found.stream().map(RouteStop::getStopName).collect(Collectors.toList())));
            }
        }

        if (dropoffId == null) {
            String dname = req.getDropoffStopName();
            if (dname != null && !dname.isBlank()) {
                List<RouteStop> found = routeStopRepository.findByStopNameContainingIgnoreCase(dname);
                if (found.isEmpty()) {
                    return ResponseEntity.badRequest().body(Map.of("error", "dropoff stop not found: " + dname));
                }
                List<RouteStop> exact = found.stream()
                        .filter(r -> r.getStopName().equalsIgnoreCase(dname))
                        .collect(Collectors.toList());
                if (exact.size() == 1) dropoffId = exact.get(0).getId();
                else if (found.size() == 1) dropoffId = found.get(0).getId();
                else return ResponseEntity.badRequest().body(Map.of("error", "dropoff stop name is ambiguous; provide the stop id", "matches", found.stream().map(RouteStop::getStopName).collect(Collectors.toList())));
            }
        }

        if (pickupId == null || dropoffId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "pickupStopId and dropoffStopId are required (or provide unambiguous stop names)"));
        }

        List<MatchedRouteDto> routes = tripMatchingService.matchTrip(pickupId, dropoffId);

        Map<String, Object> resp = new HashMap<>();
        resp.put("routes", routes);

        return ResponseEntity.ok(resp);
    }
}
