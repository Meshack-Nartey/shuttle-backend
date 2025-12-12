package com.shuttlebackend.mappers;

import com.shuttlebackend.dtos.RegisterRouteRequest;
import com.shuttlebackend.dtos.RouteDto;
import com.shuttlebackend.entities.Route;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface RouteMapper {

    // Map entity.id → dto.routeId and entity.school.id → dto.schoolId
    @Mapping(source = "id", target = "routeId")
    @Mapping(source = "school.id", target = "schoolId")
    RouteDto toDto(Route entity);

    // Map dto.routeId → entity.id and dto.schoolId → entity.school.id
    @Mapping(source = "routeId", target = "id")
    @Mapping(source = "schoolId", target = "school.id")
    Route toEntity(RouteDto dto);

    // RegisterRouteRequest has no ID, so no mapping needed
    Route toEntity(RegisterRouteRequest request);

    @Mapping(source = "schoolId", target = "id", ignore = true)
    void update(RegisterRouteRequest request, @MappingTarget Route entity);
}
