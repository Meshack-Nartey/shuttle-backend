package com.shuttlebackend.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRouteStopRequest {
    private Integer routeId;
    private String stopName;
    private Double latitude;
    private Double longitude;
    private Integer stopOrder;
}
