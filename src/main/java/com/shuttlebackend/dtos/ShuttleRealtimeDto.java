package com.shuttlebackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class ShuttleRealtimeDto {
    private Integer shuttleId;
    private Double latitude;
    private Double longitude;
    private String direction; // forward/backward/stationary
    private Double speedKph;
    private String nextStop; // stop name
    private Instant lastUpdated;
}

