package com.shuttlebackend.controllers;

import com.shuttlebackend.dtos.RouteResponseDto;
import com.shuttlebackend.services.RouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;

    @GetMapping("/{shuttleId}")
    public ResponseEntity<?> getRoute(@PathVariable Long shuttleId) {
        Optional<RouteResponseDto> dto = routeService.getPointsForShuttle(shuttleId);
        return dto.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
