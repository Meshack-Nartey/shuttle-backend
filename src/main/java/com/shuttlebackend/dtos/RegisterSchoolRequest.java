package com.shuttlebackend.dtos;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class RegisterSchoolRequest {
    private String schoolName;
    private BigDecimal mapCenterLat;
    private BigDecimal mapCenterLon;
    private String mapImageUrl;
}
