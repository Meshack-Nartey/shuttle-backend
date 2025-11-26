package com.shuttlebackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShuttleLocationDto {
    private Integer shuttleId;
    private Double latitude;
    private Double longitude;
    private Instant createdAt;
}

