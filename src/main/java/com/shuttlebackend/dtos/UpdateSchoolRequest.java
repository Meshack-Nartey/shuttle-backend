package com.shuttlebackend.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UpdateSchoolRequest {

    @NotBlank
    @Size(max = 100)
    private String schoolName;

    @NotNull
    private BigDecimal mapCenterLat;

    @NotNull
    private BigDecimal mapCenterLon;

    @NotBlank
    private String mapImageUrl;
}
