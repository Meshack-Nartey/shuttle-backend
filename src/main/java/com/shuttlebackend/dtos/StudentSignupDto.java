package com.shuttlebackend.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudentSignupDto {
    private String studentIdNumber;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private Long schoolId;
}
