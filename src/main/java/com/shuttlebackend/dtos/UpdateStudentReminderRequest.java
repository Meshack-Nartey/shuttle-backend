package com.shuttlebackend.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateStudentReminderRequest {
    private Integer reminderTimeOffset;
    private Boolean isActive;
}
