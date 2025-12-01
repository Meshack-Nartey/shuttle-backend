package com.shuttlebackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MatchedShuttleDto {
    private Integer shuttleId;
    private String shuttleExternalId; // new: external id / shuttle name
    private String status;            // new: Running / Not Running (or raw server status)
    private Double latitude;
    private Double longitude;
    private Long etaToPickup; // seconds (nullable)
}
