package com.shuttlebackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class EndSessionResponse {
    private Integer sessionId;
    private Instant endedAt;
}

