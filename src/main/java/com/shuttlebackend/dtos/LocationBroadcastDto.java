package com.shuttlebackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class LocationBroadcastDto {
    private Integer shuttleId;
    private Double latitude;
    private Double longitude;
    private Instant createdAt;
}

