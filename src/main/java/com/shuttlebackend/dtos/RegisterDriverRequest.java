package com.shuttlebackend.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterDriverRequest {
    private String firstName;
    private String lastName;
    private String schoolName;
    private String email;
    private String password;
}
