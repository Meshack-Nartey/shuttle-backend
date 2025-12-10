package com.shuttlebackend.controllers.admin;

import com.shuttlebackend.dtos.RegisterSchoolRequest;
import com.shuttlebackend.dtos.SchoolDto;
import com.shuttlebackend.dtos.UpdateSchoolRequest;
import com.shuttlebackend.entities.School;
import com.shuttlebackend.mappers.SchoolMapper;
import com.shuttlebackend.repositories.SchoolRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

// OpenAPI
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/admin/schools")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Schools", description = "Admin CRUD for schools (ADMIN only)")
public class SchoolAdminController {

    private final SchoolRepository schoolRepo;
    private final SchoolMapper schoolMapper;

    /**
     * Create a new school
     */
    @Operation(summary = "Create school", description = "Create a school.")
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody RegisterSchoolRequest req) {
        // Prevent duplicate school names
        if (schoolRepo.findBySchoolNameIgnoreCaseTrim(req.getSchoolName()).isPresent()) {
            return ResponseEntity.badRequest().body("School already exists");
        }

        School school = new School();
        school.setSchoolName(req.getSchoolName().trim());
        school.setMapCenterLat(req.getMapCenterLat());
        school.setMapCenterLon(req.getMapCenterLon());
        school.setMapImageUrl(req.getMapImageUrl());
        school.setCreatedAt(Instant.now());

        School saved = schoolRepo.save(school);
        SchoolDto dto = schoolMapper.toDto(saved);

        return ResponseEntity.status(201).body(dto);
    }

    /**
     * Get all schools
     */
    @Operation(summary = "List schools", description = "Return all schools")
    @GetMapping
    public ResponseEntity<?> list() {
        List<SchoolDto> list = schoolRepo.findAll()
                .stream()
                .map(schoolMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(list);
    }

    /**
     * Update a school
     */
    @Operation(summary = "Update school", description = "Update school metadata by ID")
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@Parameter(description = "School ID") @PathVariable Integer id,
                                    @Valid @RequestBody UpdateSchoolRequest req) {

        School school = schoolRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("School not found"));

        school.setSchoolName(req.getSchoolName().trim());
        school.setMapCenterLat(req.getMapCenterLat());
        school.setMapCenterLon(req.getMapCenterLon());
        school.setMapImageUrl(req.getMapImageUrl());

        School saved = schoolRepo.save(school);
        SchoolDto dto = schoolMapper.toDto(saved);

        return ResponseEntity.ok(dto);
    }

    /**
     * Delete a school
     */
    @Operation(summary = "Delete school", description = "Delete a school by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@Parameter(description = "School ID") @PathVariable Integer id) {
        if (!schoolRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        schoolRepo.deleteById(id);
        return ResponseEntity.ok("School deleted successfully");
    }
}
