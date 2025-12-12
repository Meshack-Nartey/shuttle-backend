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
    private String externalId;
    private boolean inUse;

}
