package com.shuttlebackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RouteResponseDto {
    private Integer routeId;
    private String routeName;
    private List<List<Double>> polyline;
    private List<StopDto> stops;
}

