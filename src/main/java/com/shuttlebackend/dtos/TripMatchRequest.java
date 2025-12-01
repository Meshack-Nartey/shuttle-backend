package com.shuttlebackend.dtos;

import lombok.Data;

@Data
public class TripMatchRequest {
    private Integer pickupStopId;
    private Integer dropoffStopId;

    // optional names - frontend may send selected stop names instead of IDs
    private String pickupStopName;
    private String dropoffStopName;
}
