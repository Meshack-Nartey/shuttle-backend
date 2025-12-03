package com.shuttlebackend.dtos;

import lombok.Data;

@Data
public class TripReminderRequestDto {
    private Integer studentId;
    private Integer shuttleId;
    private Integer pickupStopId;
    private Integer dropoffStopId;
    private Integer reminderOffsetMinutes; // allowed values: 3,5,10,15
}

