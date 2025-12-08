package com.shuttlebackend.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateRouteRequest {
    @NotBlank
    @Size(max = 100)
    private String routeName;

    private String description;

    @NotNull
    private Integer schoolId;
}

