package com.shuttlebackend.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SchoolDto {
    private Long schoolId;
    private String schoolName;
    private Double mapCenterLat;
    private Double mapCenterLon;
    private String mapImageUrl;
}
