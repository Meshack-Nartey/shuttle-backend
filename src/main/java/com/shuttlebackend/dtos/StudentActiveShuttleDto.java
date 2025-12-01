package com.shuttlebackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class StudentActiveShuttleDto {
    private String shuttleExternalId;
    private Double latitude;
    private Double longitude;
    private String routeName; // may be null
    private String status; // Running / Not Running
    private Instant updatedAt;
}

