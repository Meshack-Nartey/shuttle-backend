package com.shuttlebackend.mappers;

import com.shuttlebackend.dtos.LocationUpdateDto;
import com.shuttlebackend.entities.LocationUpdate;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LocationUpdateMapper {

    LocationUpdateDto toDto(LocationUpdate entity);

    LocationUpdate toEntity(LocationUpdateDto dto);
}
