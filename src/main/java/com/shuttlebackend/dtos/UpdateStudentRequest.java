package com.shuttlebackend.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UpdateStudentRequest {
    private String firstName;
    private String lastName;
    private Integer schoolId;
}
