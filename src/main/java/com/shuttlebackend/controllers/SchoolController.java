package com.shuttlebackend.controllers;

import com.shuttlebackend.dtos.RegisterSchoolRequest;
import com.shuttlebackend.dtos.SchoolDto;
import com.shuttlebackend.entities.School;
import com.shuttlebackend.repositories.SchoolRepository;
import com.shuttlebackend.mappers.SchoolMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/schools")
@RequiredArgsConstructor
public class SchoolController {

    private final SchoolRepository schoolRepo;
    private final SchoolMapper schoolMapper;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody RegisterSchoolRequest req) {
        School school = new School();
        school.setSchoolName(req.getSchoolName());
        school.setMapCenterLat(req.getMapCenterLat());
        school.setMapCenterLon(req.getMapCenterLon());
        school.setMapImageUrl(req.getMapImageUrl());
        school.setCreatedAt(Instant.now());

        School saved = schoolRepo.save(school);
        SchoolDto dto = schoolMapper.toDto(saved);

        return ResponseEntity.status(201).body(dto);
    }

    @GetMapping
    public ResponseEntity<?> all() {
        List<SchoolDto> list = schoolRepo.findAll().stream().map(schoolMapper::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }
}
