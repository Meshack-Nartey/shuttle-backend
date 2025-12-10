package com.shuttlebackend.controllers.admin;

import com.shuttlebackend.dtos.RegisterShuttleRequest;
import com.shuttlebackend.dtos.ShuttleDto;
import com.shuttlebackend.entities.School;
import com.shuttlebackend.entities.Shuttle;
import com.shuttlebackend.repositories.ShuttleRepository;
import com.shuttlebackend.services.ShuttleService;
import com.shuttlebackend.services.SchoolService;
import com.shuttlebackend.utils.ApiResponse;
import com.shuttlebackend.mappers.ShuttleMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

// OpenAPI
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/admin/shuttles")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Shuttles", description = "Admin CRUD for shuttles (ADMIN only)")
public class ShuttleAdminController {

    private final ShuttleRepository shuttleRepository;
    private final ShuttleService shuttleService;
    private final SchoolService schoolService;
    private final ShuttleMapper shuttleMapper;

    @Operation(summary = "List shuttles", description = "List all shuttles")
    @GetMapping
    public ResponseEntity<?> list() {
        List<ShuttleDto> dtos = shuttleRepository.findAll().stream()
                .map(shuttleMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse<>(true, dtos));
    }

    @Operation(summary = "Create shuttle", description = "Create a new shuttle.")
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody RegisterShuttleRequest req) {
        School s = schoolService.findById(req.getSchoolId()).orElseThrow(() -> new RuntimeException("School not found"));
        Shuttle shuttle = new Shuttle();
        shuttle.setLicensePlate(req.getLicensePlate());
        shuttle.setCapacity(req.getCapacity());
        shuttle.setStatus(req.getStatus() == null ? "Available" : req.getStatus());
        shuttle.setSchool(s);
        Shuttle saved = shuttleService.create(shuttle);
        ShuttleDto dto = shuttleMapper.toDto(saved);
        return ResponseEntity.status(201).body(new ApiResponse<>(true, dto));
    }

    @Operation(summary = "Update shuttle", description = "Update shuttle metadata by ID")
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@Parameter(description = "Shuttle ID") @PathVariable Integer id, @Valid @RequestBody RegisterShuttleRequest req) {
        Shuttle shuttle = shuttleService.findById(id).orElseThrow(() -> new RuntimeException("Shuttle not found"));
        if (req.getLicensePlate() != null) shuttle.setLicensePlate(req.getLicensePlate());
        if (req.getCapacity() != null) shuttle.setCapacity(req.getCapacity());
        if (req.getStatus() != null) shuttle.setStatus(req.getStatus());
        if (req.getSchoolId() != null) {
            School s = schoolService.findById(req.getSchoolId()).orElseThrow(() -> new RuntimeException("School not found"));
            shuttle.setSchool(s);
        }
        Shuttle saved = shuttleService.create(shuttle);
        ShuttleDto dto = shuttleMapper.toDto(saved);
        return ResponseEntity.ok(new ApiResponse<>(true, dto));
    }

    @Operation(summary = "Delete shuttle", description = "Delete a shuttle by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@Parameter(description = "Shuttle ID") @PathVariable Integer id) {
        if (!shuttleRepository.existsById(id)) return ResponseEntity.notFound().build();
        shuttleRepository.deleteById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Deleted"));
    }
}
