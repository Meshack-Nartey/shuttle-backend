package com.shuttlebackend.mappers;

import com.shuttlebackend.dtos.*;
import com.shuttlebackend.entities.Shuttle;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ShuttleMapper {

    @Mapping(source = "id", target = "shuttleId")
    @Mapping(source = "school.id", target = "schoolId")
    @Mapping(source = "externalId", target = "externalId")
    ShuttleDto toDto(Shuttle shuttle);

    @Mapping(source = "shuttleId", target = "id")
    @Mapping(source = "schoolId", target = "school.id")
    @Mapping(source = "externalId", target = "externalId")
    Shuttle toEntity(ShuttleDto dto);

    @Mapping(source = "schoolId", target = "school.id")
    Shuttle toEntity(RegisterShuttleRequest request);

    void update(RegisterShuttleRequest request, @MappingTarget Shuttle shuttle);
}
