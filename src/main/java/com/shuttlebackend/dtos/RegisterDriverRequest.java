package com.shuttlebackend.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterDriverRequest {
    private String firstName;
    private String lastName;
    // Accept external school identifier (e.g., "KNUST654") from frontend
    private String schoolId;
    private String email;
    private String password;
}
