package com.shuttlebackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StopSearchDto {
    private Integer stopId;
    private String stopName;
    private Double latitude;
    private Double longitude;
}

