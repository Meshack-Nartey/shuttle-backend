package com.shuttlebackend.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RegisterStudentRequest {
    private String studentIdNumber;
    private String firstName;
    private String lastName;
    // The display name of the school as sent by the frontend (e.g., "KNUST").
    // The backend will resolve this to the internal school.id (case-insensitive, trimmed).
    private String schoolName;
    // Optional username that the student can provide for display purposes
    private String username;

    // user signup fields
    private String email;
    private String password;
}
