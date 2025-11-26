package com.shuttlebackend.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudentReminderDto {
    private Integer reminderId;
    private Integer studentId;
    private Integer routeId;
    private Integer targetStopId;
    private Integer reminderTimeOffset;
    private Boolean isActive;
    private String createdAt;
}
