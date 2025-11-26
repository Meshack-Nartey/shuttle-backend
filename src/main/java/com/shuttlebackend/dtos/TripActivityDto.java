package com.shuttlebackend.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TripActivityDto {
    private Long tripId;
    private Integer studentId;
    private Integer shuttleId;
    private Integer departureStopId;
    private Integer arrivalStopId;
    private Integer routeId;
    private String estimatedTime;
    private String actualTime;
    private String status;
}
