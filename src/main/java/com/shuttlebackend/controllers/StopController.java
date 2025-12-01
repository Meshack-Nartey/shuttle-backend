package com.shuttlebackend.controllers;

import com.shuttlebackend.dtos.StopSearchDto;
import com.shuttlebackend.entities.RouteStop;
import com.shuttlebackend.repositories.RouteStopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/stops")
@RequiredArgsConstructor
public class StopController {

    private final RouteStopRepository routeStopRepository;

    @GetMapping("/search")
    public ResponseEntity<?> searchStops(@RequestParam("q") String q) {
        if (q == null || q.isBlank()) return ResponseEntity.badRequest().body("q is required");

        List<RouteStop> stops = routeStopRepository.findByStopNameContainingIgnoreCase(q);

        List<StopSearchDto> dto = stops.stream().map(s -> new StopSearchDto(
                s.getId(), s.getStopName(), s.getLatitude() != null ? s.getLatitude().doubleValue() : null,
                s.getLongitude() != null ? s.getLongitude().doubleValue() : null
        )).collect(Collectors.toList());

        return ResponseEntity.ok(dto);
    }
}

