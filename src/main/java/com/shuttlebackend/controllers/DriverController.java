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

// OpenAPI
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@Tag(name = "Driver", description = "Driver endpoints for driver app: start/end sessions, update location, list shuttles and routes")
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

    // --------------------------------------------------------------------
    // GET SHUTTLES FOR DRIVER'S SCHOOL  (UPDATED WITH inUse LOGIC)
    // --------------------------------------------------------------------
    @Operation(summary = "List shuttles for the authenticated driver's school",
            description = "Returns a list of shuttles assigned to the driver's school. Includes inUse flag so UI can disable shuttles currently in active sessions.")
    @GetMapping("/shuttles")
    public ResponseEntity<ApiResponse<?>> getShuttlesForDriver() {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Driver driver = driverService.findDriverByEmail(email)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        List<Shuttle> shuttles = shuttleService.findAllBySchool(driver.getSchool().getId());

        // Map shuttles to DTOs and append inUse flag
        List<ShuttleDto> dtos = shuttles.stream()
                .map(shuttle -> {
                    ShuttleDto dto = shuttleMapper.toDto(shuttle);
                    dto.setInUse(sessionService.findActiveSessionByShuttle(shuttle.getId()).isPresent());
                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ApiResponse<>(true, dtos));
    }

    // --------------------------------------------------------------------
    // START SESSION
    // --------------------------------------------------------------------
    @Operation(summary = "Start driver session",
            description = "Start a new driver session for a shuttle }. Prevents starting a session on a shuttle already in use.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Session started", content = @Content(schema = @Schema(implementation = DriverSessionResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Driver already has an active session")
    })
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
            route = routeService.findById(req.getRouteId())
                    .orElseThrow(() -> new RuntimeException("Route not found"));
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
            return ResponseEntity.status(409)
                    .body(new ApiResponse<>(false, ex.getMessage()));
        }
    }

    // --------------------------------------------------------------------
    // GET ROUTES FOR DRIVER'S SCHOOL
    // --------------------------------------------------------------------
    @Operation(summary = "List routes for the authenticated driver's school",
            description = "Returns all routes associated with the driver's school.")
    @GetMapping("/routes")
    public ResponseEntity<?> getRoutesForDriver() {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Driver driver = driverService.findDriverByEmail(email)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        var routes = routeService.findBySchool(driver.getSchool().getId());

        // Map to DTOs, filter out any with null IDs (prevents null/duplicate keys on frontend),
        // and deduplicate by routeId while preserving order.
        var mapped = routes.stream()
                .map(routeMapper::toDto)
                .filter(dto -> dto != null && dto.getRouteId() != null)
                .collect(Collectors.toList());

        List<RouteDto> unique = new java.util.ArrayList<>();
        java.util.Set<Integer> seen = new java.util.HashSet<>();
        for (RouteDto d : mapped) {
            if (seen.add(d.getRouteId())) {
                unique.add(d);
            }
        }

        // Return shape { "data": [ ... ] } as required by frontend
        return ResponseEntity.ok(java.util.Map.of("data", unique));
    }

    // --------------------------------------------------------------------
    // UPDATE LOCATION
    // --------------------------------------------------------------------
    @Operation(summary = "Update driver location",
            description = "Driver posts periodic location updates.)
    @PostMapping("/location")
    public ResponseEntity<ApiResponse<?>> updateLocation(@Valid @RequestBody LocationUpdateDto dto) {

        Shuttle shuttle = shuttleService.findById(dto.getShuttleId())
                .orElseThrow(() -> new RuntimeException("Shuttle not found"));

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Driver driver = driverService.findDriverByEmail(email)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        var active = sessionService.findActiveSessionByShuttle(shuttle.getId())
                .orElseThrow(() -> new RuntimeException("No active session for this shuttle"));

        if (!active.getDriver().getId().equals(driver.getId())) {
            return ResponseEntity.status(403)
                    .body(new ApiResponse<>(false, "Driver not active on this shuttle"));
        }

        LocationUpdate lu = locationUpdateMapper.toEntity(dto);
        lu.setShuttle(shuttle);

        LocationUpdate saved = locationUpdateService.save(lu);
        LocationUpdateDto out = locationUpdateMapper.toDto(saved);

        return ResponseEntity.ok(new ApiResponse<>(true, out));
    }

    // --------------------------------------------------------------------
    // END SESSION
    // --------------------------------------------------------------------
    @Operation(summary = "End driver session",
            description = "End an active driver session. Ends either a specific session or the active session for the driver.")
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

            EndSessionResponse resp = new EndSessionResponse(
                    ended.getId(),
                    ended.getEndedAt()
            );

            return ResponseEntity.ok(new ApiResponse<>(true, resp));

        } catch (RuntimeException ex) {
            return ResponseEntity.status(400)
                    .body(new ApiResponse<>(false, ex.getMessage()));
        }
    }
}
