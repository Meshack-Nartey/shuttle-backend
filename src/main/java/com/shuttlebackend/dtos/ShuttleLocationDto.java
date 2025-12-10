package com.shuttlebackend.dtos;

import lombok.Data;
import java.time.Instant;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

@Data
public class ShuttleLocationDto {
    // Use Integer to match entity ids across the codebase
    private Integer shuttleId;

    @NotNull
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    private Double latitude;

    @NotNull
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    private Double longitude;

    private Instant createdAt; // ISO timestamp
}
