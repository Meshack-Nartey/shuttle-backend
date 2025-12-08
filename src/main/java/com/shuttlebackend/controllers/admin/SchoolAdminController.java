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

@RestController
@RequestMapping("/admin/schools")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SchoolAdminController {

    private final SchoolRepository schoolRepo;
    private final SchoolMapper schoolMapper;

    /**
     * Create a new school
     */
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
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id,
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
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        if (!schoolRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        schoolRepo.deleteById(id);
        return ResponseEntity.ok("School deleted successfully");
    }
}
