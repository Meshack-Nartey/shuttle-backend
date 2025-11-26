package com.shuttlebackend.dtos;
import lombok.*;

@Getter @Setter
public class RouteDto {
    private Integer routeId;
    private String routeName;
    private String description;
    private Integer schoolId;
}
