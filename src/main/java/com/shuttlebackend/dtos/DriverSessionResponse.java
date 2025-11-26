package com.shuttlebackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class DriverSessionResponse {
    private Integer sessionId;
    private Integer shuttleId;
    private Instant startedAt;
}

