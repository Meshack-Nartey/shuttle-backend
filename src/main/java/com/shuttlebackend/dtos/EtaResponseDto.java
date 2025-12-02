package com.shuttlebackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EtaResponseDto {
    private Integer shuttleId;
    private Integer pickupStopId;
    private Integer dropoffStopId;

    private Long etaMillis;
    private String etaTimestamp; // ISO

    private Double distanceShuttleToPickup;
    private Double distancePickupToDrop;
    private Double totalDistance;

    private String direction;
    private Double speedKph;

    private Integer shuttleSegmentIndex;
    private Integer pickupSegmentIndex;
    private Integer dropoffSegmentIndex;

    private String debug;
}
