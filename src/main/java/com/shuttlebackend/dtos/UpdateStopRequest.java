package com.shuttlebackend.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateStopRequest {
    @NotBlank
    @Size(max = 100)
    private String stopName;

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;

    @NotNull
    private Integer stopOrder;

    @NotBlank
    private String direction;
}

