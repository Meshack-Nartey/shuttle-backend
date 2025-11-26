package com.shuttlebackend.dtos;

import lombok.*;

@Getter @Setter
public class RegisterRouteRequest {
    private String routeName;
    private String description;
    private Long schoolId;
}
