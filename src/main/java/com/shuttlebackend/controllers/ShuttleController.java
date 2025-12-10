package com.shuttlebackend.controllers;

import com.shuttlebackend.dtos.RegisterShuttleRequest;
import com.shuttlebackend.dtos.ShuttleDto;
import com.shuttlebackend.dtos.ShuttleLocationDto;
import com.shuttlebackend.entities.Shuttle;
import com.shuttlebackend.services.ShuttleService;
import com.shuttlebackend.services.SchoolService;
import com.shuttlebackend.utils.ApiResponse;
import com.shuttlebackend.mappers.ShuttleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// OpenAPI
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequiredArgsConstructor
@RequestMapping("/shuttles")
@Tag(name = "Shuttles", description = "Shuttle management and registration endpoints")
public class ShuttleController {

    private final ShuttleService shuttleService;
    private final SchoolService schoolService;
    private final ShuttleMapper shuttleMapper;

    @Operation(summary = "Register a new shuttle")
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<?>> createShuttle(@RequestBody RegisterShuttleRequest req) {

        Shuttle shuttle = new Shuttle();
        shuttle.setLicensePlate(req.getLicensePlate());
        shuttle.setCapacity(req.getCapacity());
        shuttle.setStatus(req.getStatus() == null ? "Available" : req.getStatus());
        shuttle.setSchool(
                schoolService.findById(req.getSchoolId())
                        .orElseThrow(() -> new RuntimeException("School not found"))
        );

        Shuttle saved = shuttleService.create(shuttle);

        ShuttleDto dto = shuttleMapper.toDto(saved);

        return ResponseEntity.status(201)
                .body(new ApiResponse<>(true, dto));
    }

    @Operation(summary = "Get shuttle current location",
            description = "Returns the most recent latitude/longitude and timestamp for a shuttle. Useful for frontend maps and real-time tracking.")
    @GetMapping("/{id}/location")
    public ResponseEntity<ApiResponse<ShuttleLocationDto>> getShuttleLocation(@PathVariable("id") Integer id) {
        Shuttle shuttle = shuttleService.findById(id).orElseThrow(() -> new RuntimeException("Shuttle not found"));

        // Try to get latest location updates (most recent first)
        var updates = shuttle.getLocationUpdates();
        ShuttleLocationDto dto = new ShuttleLocationDto();
        dto.setShuttleId(shuttle.getId());

        if (updates == null || updates.isEmpty()) {
            dto.setLatitude(null);
            dto.setLongitude(null);
            dto.setCreatedAt(null);
        } else {
            // locationUpdates is a Set, attempt to pick the latest by createdAt
            var latest = updates.stream()
                    .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                    .findFirst()
                    .orElse(null);
            if (latest != null) {
                dto.setLatitude(latest.getLatitude().doubleValue());
                dto.setLongitude(latest.getLongitude().doubleValue());
                dto.setCreatedAt(latest.getCreatedAt());
            }
        }

        return ResponseEntity.ok(new ApiResponse<>(true, dto));
    }
}
