package com.shuttlebackend.controllers.admin;

import com.shuttlebackend.dtos.CreateRouteRequest;
import com.shuttlebackend.dtos.UpdateRouteRequest;
import com.shuttlebackend.entities.Route;
import com.shuttlebackend.entities.School;
import com.shuttlebackend.repositories.RouteRepository;
import com.shuttlebackend.repositories.SchoolRepository;
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
@RequestMapping("/admin/routes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Routes", description = "Admin CRUD for routes (ADMIN only)")
public class RouteAdminController {

    private final RouteRepository routeRepository;
    private final SchoolRepository schoolRepository;

    @Operation(summary = "Create route", description = "Create a new route under a school. Body: CreateRouteRequest { routeName, description, schoolId }")
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CreateRouteRequest req) {
        School s = schoolRepository.findById(req.getSchoolId()).orElseThrow(() -> new RuntimeException("School not found"));
        Route r = new Route();
        r.setRouteName(req.getRouteName());
        r.setDescription(req.getDescription());
        r.setSchool(s);
        Route saved = routeRepository.save(r);
        return ResponseEntity.ok(saved);
    }

    @Operation(summary = "Update route", description = "Update route metadata by ID")
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@Parameter(description = "Route ID") @PathVariable Integer id, @Valid @RequestBody UpdateRouteRequest req) {
        Route r = routeRepository.findById(id).orElseThrow(() -> new RuntimeException("Route not found"));
        r.setRouteName(req.getRouteName());
        r.setDescription(req.getDescription());
        Route saved = routeRepository.save(r);
        return ResponseEntity.ok(saved);
    }

    @Operation(summary = "Delete route", description = "Delete route by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@Parameter(description = "Route ID") @PathVariable Integer id) {
        if (!routeRepository.existsById(id)) return ResponseEntity.notFound().build();
        routeRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
