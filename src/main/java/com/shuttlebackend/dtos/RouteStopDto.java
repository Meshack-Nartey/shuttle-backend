package com.shuttlebackend.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RouteStopDto {
    private Integer routeStopId;
    private Integer routeId;
    private String stopName;
    private Double latitude;
    private Double longitude;
    private Integer stopOrder;
}
