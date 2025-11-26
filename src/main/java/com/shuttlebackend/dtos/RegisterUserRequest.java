package com.shuttlebackend.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RegisterUserRequest {
    private String email;
    private String password;
    private String role; // ROLE_STUDENT, ROLE_DRIVER, ROLE_ADMIN
}
