package com.shuttlebackend.controllers;

import com.shuttlebackend.dtos.RegisterShuttleRequest;
import com.shuttlebackend.dtos.ShuttleDto;
import com.shuttlebackend.entities.Shuttle;
import com.shuttlebackend.services.ShuttleService;
import com.shuttlebackend.services.SchoolService;
import com.shuttlebackend.utils.ApiResponse;
import com.shuttlebackend.mappers.ShuttleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/shuttles")
public class ShuttleController {

    private final ShuttleService shuttleService;
    private final SchoolService schoolService;
    private final ShuttleMapper shuttleMapper;

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
}
