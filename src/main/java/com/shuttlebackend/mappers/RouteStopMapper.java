package com.shuttlebackend.mappers;

import com.shuttlebackend.dtos.RegisterRouteStopRequest;
import com.shuttlebackend.dtos.RouteStopDto;
import com.shuttlebackend.entities.RouteStop;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface RouteStopMapper {

    RouteStopDto toDto(RouteStop entity);

    RouteStop toEntity(RouteStopDto dto);

    RouteStop toEntity(RegisterRouteStopRequest request);

    void update(RegisterRouteStopRequest request, @MappingTarget RouteStop entity);
}
