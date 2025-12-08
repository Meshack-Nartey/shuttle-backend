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

@RestController
@RequestMapping("/admin/stops")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class StopAdminController {

    private final RouteStopRepository routeStopRepository;
    private final RouteRepository routeRepository;

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

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @Valid @RequestBody UpdateStopRequest req) {
        RouteStop s = routeStopRepository.findById(id).orElseThrow(() -> new RuntimeException("Stop not found"));
        s.setStopName(req.getStopName());
        s.setLatitude(new java.math.BigDecimal(req.getLatitude()));
        s.setLongitude(new java.math.BigDecimal(req.getLongitude()));
        s.setStopOrder(req.getStopOrder());
        s.setDirection(req.getDirection());
        RouteStop saved = routeStopRepository.save(s);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        if (!routeStopRepository.existsById(id)) return ResponseEntity.notFound().build();
        routeStopRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}

