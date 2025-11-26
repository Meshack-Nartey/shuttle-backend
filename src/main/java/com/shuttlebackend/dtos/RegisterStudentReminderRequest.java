package com.shuttlebackend.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterStudentReminderRequest {
    private Integer studentId;
    private Integer routeId;
    private Integer targetStopId;
    private Integer reminderTimeOffset;
}
