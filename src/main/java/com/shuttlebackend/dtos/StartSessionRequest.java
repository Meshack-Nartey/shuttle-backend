package com.shuttlebackend.dtos;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;

@Getter @Setter
public class StartSessionRequest {
    @NotNull
    private Integer shuttleId;

    @NotNull
    private Integer routeId; // now mandatory: driver must select a route
}
