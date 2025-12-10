package com.shuttlebackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class LocationBroadcastDto {
    private Integer shuttleId;
    private Double lat;
    private Double lng;
    private Instant timestamp;
}
