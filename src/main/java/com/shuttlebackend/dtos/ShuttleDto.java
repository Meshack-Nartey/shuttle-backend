package com.shuttlebackend.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ShuttleDto {
    private Integer shuttleId;
    private String licensePlate;
    private Integer capacity;
    private String status;
    private Integer schoolId;
}
