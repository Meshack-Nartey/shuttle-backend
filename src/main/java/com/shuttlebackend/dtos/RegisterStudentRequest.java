package com.shuttlebackend.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RegisterStudentRequest {
    private String studentIdNumber;
    private String firstName;
    private String lastName;
    private Integer schoolId;

    // user signup fields
    private String email;
    private String password;
}
