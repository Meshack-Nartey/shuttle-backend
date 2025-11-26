package com.shuttlebackend.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterDriverRequest {
    private String firstName;
    private String lastName;
    private Integer schoolId;
    private String email;
    private String password;
}
