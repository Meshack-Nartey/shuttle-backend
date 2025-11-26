package com.shuttlebackend.mappers;

import com.shuttlebackend.dtos.TripActivityDto;
import com.shuttlebackend.entities.TripActivity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TripActivityMapper {

    TripActivityDto toDto(TripActivity entity);

    TripActivity toEntity(TripActivityDto dto);
}
