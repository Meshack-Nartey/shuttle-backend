package com.shuttlebackend.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class DriverDto {
    private Integer driverId;
    private Integer userId;
    private String carNumber;
    private String firstName;
    private String lastName;
    private Integer schoolId;
}
