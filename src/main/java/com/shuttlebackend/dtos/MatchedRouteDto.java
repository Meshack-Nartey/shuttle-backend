package com.shuttlebackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class MatchedRouteDto {
    private Integer routeId;
    private String routeName;
    private List<MatchedShuttleDto> activeShuttles;
}

