package com.shuttlebackend.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class JwtRequest {
    private String email;       // for students/admins (optional for drivers)
    private String password;
    private String loginType;   // optional: "driver" or "student"

}
