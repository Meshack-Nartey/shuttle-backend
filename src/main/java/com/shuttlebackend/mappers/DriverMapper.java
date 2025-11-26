package com.shuttlebackend.mappers;

import com.shuttlebackend.dtos.*;
import com.shuttlebackend.entities.Driver;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface DriverMapper {

    DriverDto toDto(Driver driver);

    Driver toEntity(DriverDto dto);

    Driver toEntity(RegisterDriverRequest request);

    void update(UpdateDriverRequest request, @MappingTarget Driver driver);
}
