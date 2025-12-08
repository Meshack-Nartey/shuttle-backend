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

@RestController
@RequestMapping("/admin/routes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class RouteAdminController {

    private final RouteRepository routeRepository;
    private final SchoolRepository schoolRepository;

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

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @Valid @RequestBody UpdateRouteRequest req) {
        Route r = routeRepository.findById(id).orElseThrow(() -> new RuntimeException("Route not found"));
        r.setRouteName(req.getRouteName());
        r.setDescription(req.getDescription());
        Route saved = routeRepository.save(r);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        if (!routeRepository.existsById(id)) return ResponseEntity.notFound().build();
        routeRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}

