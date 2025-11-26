package com.shuttlebackend.mappers;

import com.shuttlebackend.dtos.RegisterRouteRequest;
import com.shuttlebackend.dtos.RouteDto;
import com.shuttlebackend.entities.Route;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface RouteMapper {

    RouteDto toDto(Route entity);

    Route toEntity(RouteDto dto);

    Route toEntity(RegisterRouteRequest request);

    void update(RegisterRouteRequest request, @MappingTarget Route entity);
}
