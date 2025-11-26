package com.shuttlebackend.controllers;

import com.shuttlebackend.dtos.StartSessionRequest;
import com.shuttlebackend.dtos.DriverSessionResponse;
import com.shuttlebackend.dtos.ShuttleDto;
import com.shuttlebackend.entities.Driver;
import com.shuttlebackend.entities.Shuttle;
import com.shuttlebackend.entities.DriverSession;
import com.shuttlebackend.entities.Route;
import com.shuttlebackend.entities.LocationUpdate;
import com.shuttlebackend.services.DriverService;
import com.shuttlebackend.services.ShuttleService;
import com.shuttlebackend.services.DriverSessionService;
import com.shuttlebackend.services.RouteService;
import com.shuttlebackend.services.LocationUpdateService;
import com.shuttlebackend.utils.ApiResponse;
import com.shuttlebackend.mappers.ShuttleMapper;
import com.shuttlebackend.mappers.RouteMapper;
import com.shuttlebackend.mappers.LocationUpdateMapper;
import com.shuttlebackend.dtos.LocationUpdateDto;
import com.shuttlebackend.dtos.RouteDto;
import com.shuttlebackend.dtos.EndSessionRequest;
import com.shuttlebackend.dtos.EndSessionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/driver")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;
    private final ShuttleService shuttleService;
    private final DriverSessionService sessionService;
    private final ShuttleMapper shuttleMapper;
    private final RouteService routeService;
    private final RouteMapper routeMapper;
    private final LocationUpdateService locationUpdateService;
    private final LocationUpdateMapper locationUpdateMapper;

    // ---------------------------------------
    // GET SHUTTLES FOR DRIVER'S SCHOOL
    // ---------------------------------------
    @GetMapping("/shuttles")
    public ResponseEntity<ApiResponse<?>> getShuttlesForDriver() {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Driver driver = driverService.findDriverByEmail(email)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        List<Shuttle> shuttles = shuttleService.findAllBySchool(driver.getSchool().getId());

        List<ShuttleDto> dtos = shuttles.stream().map(shuttleMapper::toDto).collect(Collectors.toList());

        return ResponseEntity.ok(new ApiResponse<>(true, dtos));
    }


    // START SESSION
    @PostMapping("/session/start")
    public ResponseEntity<ApiResponse<?>> startSession(
            @Valid @RequestBody StartSessionRequest req
    ) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Driver driver = driverService.findDriverByEmail(email)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        Shuttle shuttle = shuttleService.findById(req.getShuttleId())
                .orElseThrow(() -> new RuntimeException("Shuttle not found"));

        Route route = null;
        if (req.getRouteId() != null) {
            route = routeService.findById(req.getRouteId()).orElseThrow(() -> new RuntimeException("Route not found"));
        }

        try {
            DriverSession session = sessionService.startSession(driver, shuttle, route);

            DriverSessionResponse resp = new DriverSessionResponse(
                    session.getId(),
                    session.getShuttle().getId(),
                    session.getStartedAt()
            );

            return ResponseEntity.ok(new ApiResponse<>(true, resp));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(409).body(new ApiResponse<>(false, ex.getMessage()));
        }
    }

    // GET ROUTES FOR DRIVER'S SCHOOL
    @GetMapping("/routes")
    public ResponseEntity<ApiResponse<?>> getRoutesForDriver() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Driver driver = driverService.findDriverByEmail(email)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        var routes = routeService.findBySchool(driver.getSchool().getId());
        var dtos = routes.stream().map(routeMapper::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse<>(true, dtos));
    }

    // UPDATE LOCATION
    @PostMapping("/location")
    public ResponseEntity<ApiResponse<?>> updateLocation(@Valid @RequestBody LocationUpdateDto dto) {
        // validate shuttle exists
        Shuttle shuttle = shuttleService.findById(dto.getShuttleId()).orElseThrow(() -> new RuntimeException("Shuttle not found"));

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Driver driver = driverService.findDriverByEmail(email).orElseThrow(() -> new RuntimeException("Driver not found"));

        // ensure driver has active session for this shuttle
        var active = sessionService.findActiveSessionByShuttle(shuttle.getId())
                .orElseThrow(() -> new RuntimeException("No active session for this shuttle"));

        if (!active.getDriver().getId().equals(driver.getId())) {
            return ResponseEntity.status(403).body(new ApiResponse<>(false, "Driver not active on this shuttle"));
        }

        LocationUpdate lu = locationUpdateMapper.toEntity(dto);
        lu.setShuttle(shuttle);

        LocationUpdate saved = locationUpdateService.save(lu);
        LocationUpdateDto out = locationUpdateMapper.toDto(saved);
        return ResponseEntity.ok(new ApiResponse<>(true, out));
    }

    // END SESSION
    @PostMapping("/session/end")
    public ResponseEntity<ApiResponse<?>> endSession(@RequestBody EndSessionRequest req) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Driver driver = driverService.findDriverByEmail(email)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        try {
            DriverSession ended;
            if (req != null && req.getSessionId() != null) {
                ended = sessionService.endSessionById(req.getSessionId());
            } else {
                ended = sessionService.endActiveSessionForDriver(driver.getId());
            }

            EndSessionResponse resp = new EndSessionResponse(ended.getId(), ended.getEndedAt());
            return ResponseEntity.ok(new ApiResponse<>(true, resp));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(400).body(new ApiResponse<>(false, ex.getMessage()));
        }
    }

}
