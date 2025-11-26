package com.shuttlebackend.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RegisterShuttleRequest {
    private String licensePlate;
    private Integer capacity;
    private String status;
    private Integer schoolId;
}
