package com.shuttlebackend.controllers;

import com.shuttlebackend.dtos.TripMatchRequest;
import com.shuttlebackend.dtos.MatchedRouteDto;
import com.shuttlebackend.services.RoutingResolverService;
import com.shuttlebackend.services.TripMatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/student/trip")
@RequiredArgsConstructor
public class StudentTripController {

    private final TripMatchingService tripMatchingService;
    private final RoutingResolverService routingResolverService;

    @PostMapping("/match")
    public ResponseEntity<?> matchTrip(@RequestBody TripMatchRequest req) {
        if (req == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "request body is required"));
        }

        Integer pickupId = req.getPickupStopId();
        Integer dropoffId = req.getDropoffStopId();
        String pickupName = req.getPickupStopName();
        String dropoffName = req.getDropoffStopName();

        // ░░░░░░ CASE 1 — USER PROVIDED BOTH IDs ░░░░░░
        if (pickupId != null && dropoffId != null) {
            List<MatchedRouteDto> routes = tripMatchingService.matchTrip(pickupId, dropoffId);
            return ResponseEntity.ok(Map.of("routes", routes));
        }

        // ░░░░░░ CASE 2 — USER PROVIDED NAMES (USE THE RESOLVER) ░░░░░░
        if (pickupId == null || dropoffId == null) {

            if (pickupName == null || pickupName.isBlank() ||
                    dropoffName == null || dropoffName.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Provide either stop IDs or non-empty stop names"
                ));
            }

            // 🔥 Full intelligent name resolution using RoutingResolverService
            Map<String, Object> result = routingResolverService.resolveByNames(pickupName, dropoffName);

            // Forward error to client if resolver failed
            if (result.containsKey("error")) {
                return ResponseEntity.badRequest().body(result);
            }

            // Extract resolved stops
            Map<String, Object> pickupResolved = (Map<String, Object>) result.get("pickupStop");
            Map<String, Object> dropoffResolved = (Map<String, Object>) result.get("dropoffStop");

            pickupId = (Integer) pickupResolved.get("stop_id");
            dropoffId = (Integer) dropoffResolved.get("stop_id");

            if (pickupId == null || dropoffId == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Resolver returned invalid stop IDs"
                ));
            }
        }

        // ░░░░░░ FINAL — MATCH TRIP USING RESOLVED IDs ░░░░░░
        List<MatchedRouteDto> routes = tripMatchingService.matchTrip(pickupId, dropoffId);
        return ResponseEntity.ok(Map.of("routes", routes));
    }
}
