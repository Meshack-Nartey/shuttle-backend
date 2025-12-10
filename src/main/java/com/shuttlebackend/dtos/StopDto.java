package com.shuttlebackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StopDto {
    private Integer stopOrder;
    private String stopName;
    private double lat;
    private double lng;
}

