package com.shuttlebackend.controllers.admin;

import com.shuttlebackend.dtos.CreateStopRequest;
import com.shuttlebackend.dtos.UpdateStopRequest;
import com.shuttlebackend.entities.Route;
import com.shuttlebackend.entities.RouteStop;
import com.shuttlebackend.repositories.RouteRepository;
import com.shuttlebackend.repositories.RouteStopRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

// OpenAPI
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/admin/stops")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Stops", description = "Admin CRUD for route stops (ADMIN only)")
public class StopAdminController {

    private final RouteStopRepository routeStopRepository;
    private final RouteRepository routeRepository;

    @Operation(summary = "Create stop", description = "Create a new stop under a route.")
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CreateStopRequest req) {
        Route r = routeRepository.findById(req.getRouteId()).orElseThrow(() -> new RuntimeException("Route not found"));
        RouteStop s = new RouteStop();
        s.setRoute(r);
        s.setStopName(req.getStopName());
        s.setLatitude(new java.math.BigDecimal(req.getLatitude()));
        s.setLongitude(new java.math.BigDecimal(req.getLongitude()));
        s.setStopOrder(req.getStopOrder());
        s.setDirection(req.getDirection());
        RouteStop saved = routeStopRepository.save(s);
        return ResponseEntity.ok(saved);
    }

    @Operation(summary = "Update stop", description = "Update stop metadata by ID")
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@Parameter(description = "Stop ID") @PathVariable Integer id, @Valid @RequestBody UpdateStopRequest req) {
        RouteStop s = routeStopRepository.findById(id).orElseThrow(() -> new RuntimeException("Stop not found"));
        s.setStopName(req.getStopName());
        s.setLatitude(new java.math.BigDecimal(req.getLatitude()));
        s.setLongitude(new java.math.BigDecimal(req.getLongitude()));
        s.setStopOrder(req.getStopOrder());
        s.setDirection(req.getDirection());
        RouteStop saved = routeStopRepository.save(s);
        return ResponseEntity.ok(saved);
    }

    @Operation(summary = "Delete stop", description = "Delete a stop by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@Parameter(description = "Stop ID") @PathVariable Integer id) {
        if (!routeStopRepository.existsById(id)) return ResponseEntity.notFound().build();
        routeStopRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
