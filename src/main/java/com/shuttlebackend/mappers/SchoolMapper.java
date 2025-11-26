package com.shuttlebackend.mappers;

import com.shuttlebackend.dtos.*;
import com.shuttlebackend.entities.School;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SchoolMapper {

    SchoolDto toDto(School school);

    School toEntity(SchoolDto dto);

    School toEntity(RegisterSchoolRequest request);

    void update(RegisterSchoolRequest request, @MappingTarget School school);
}
