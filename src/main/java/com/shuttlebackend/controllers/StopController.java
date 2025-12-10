package com.shuttlebackend.controllers;

import com.shuttlebackend.dtos.StopSearchDto;
import com.shuttlebackend.entities.RouteStop;
import com.shuttlebackend.repositories.RouteStopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

// OpenAPI
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/stops")
@RequiredArgsConstructor
@Tag(name = "Stops", description = "Stop lookup and search endpoints")
public class StopController {

    private final RouteStopRepository routeStopRepository;

    @Operation(summary = "Search stops by name")
    @GetMapping("/search")
    public ResponseEntity<?> searchStops(
            @Parameter(description = "Search query") @RequestParam("q") String q
    ) {
        if (q == null || q.isBlank()) return ResponseEntity.badRequest().body("q is required");

        List<RouteStop> stops = routeStopRepository.findByStopNameContainingIgnoreCase(q);

        List<StopSearchDto> dto = stops.stream().map(s -> new StopSearchDto(
                s.getId(),
                s.getStopName(),
                s.getLatitude() != null ? s.getLatitude().doubleValue() : null,
                s.getLongitude() != null ? s.getLongitude().doubleValue() : null
        )).collect(Collectors.toList());

        return ResponseEntity.ok(dto);
    }
}
