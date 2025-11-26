package com.shuttlebackend.mappers;

import com.shuttlebackend.dtos.*;
import com.shuttlebackend.entities.Shuttle;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ShuttleMapper {

    ShuttleDto toDto(Shuttle shuttle);

    Shuttle toEntity(ShuttleDto dto);

    Shuttle toEntity(RegisterShuttleRequest request);

    void update(RegisterShuttleRequest request, @MappingTarget Shuttle shuttle);
}
