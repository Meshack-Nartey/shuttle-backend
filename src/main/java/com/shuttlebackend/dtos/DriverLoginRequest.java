package com.shuttlebackend.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class DriverLoginRequest {
    private String carNumber;
    private String password;
}
