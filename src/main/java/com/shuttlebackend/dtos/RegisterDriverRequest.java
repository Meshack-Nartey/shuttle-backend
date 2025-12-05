package com.shuttlebackend.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterDriverRequest {
    private String firstName;
    private String lastName;
    // School display name (e.g., "KNUST"). Backend resolves to internal school id.
    private String schoolName;
    private String email;
    private String password;
}
