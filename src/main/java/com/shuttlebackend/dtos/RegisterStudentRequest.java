package com.shuttlebackend.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RegisterStudentRequest {
    private String studentIdNumber;
    private String firstName;
    private String lastName;
    // Accept external school identifier (e.g., "KNUST654") from frontend
    private String schoolId;
    // Optional username that the student can provide for display purposes
    private String username;

    // user signup fields
    private String email;
    private String password;
}
