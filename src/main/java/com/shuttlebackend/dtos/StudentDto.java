package com.shuttlebackend.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class StudentDto {
    private Long studentId;
    private Long userId;
    private String studentIdNumber;
    private String firstName;
    private String lastName;
    private Long schoolId;
}
