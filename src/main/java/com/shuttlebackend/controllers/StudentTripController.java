package com.shuttlebackend.controllers;

import com.shuttlebackend.dtos.TripActivityDto;
import com.shuttlebackend.entities.TripActivity;
import com.shuttlebackend.mappers.TripActivityMapper;
import com.shuttlebackend.services.TripActivityService;
import com.shuttlebackend.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
public class StudentTripController {

    private final TripActivityService tripActivityService;
    private final TripActivityMapper tripActivityMapper;

    @GetMapping("/{studentId}/trips")
    public ResponseEntity<ApiResponse<?>> getTripsForStudent(@PathVariable("studentId") Integer studentId) {
        List<TripActivity> activities = tripActivityService.findByStudent(studentId);
        List<TripActivityDto> dtos = activities.stream().map(tripActivityMapper::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse<>(true, dtos));
    }
}

