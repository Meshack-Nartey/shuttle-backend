package com.shuttlebackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CoordinateDto {
    private double lng;
    private double lat;
}

