package com.shuttlebackend.mappers;

import com.shuttlebackend.dtos.*;
import com.shuttlebackend.entities.Student;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface StudentMapper {

    StudentDto toDto(Student student);

    Student toEntity(StudentDto dto);

    Student toEntity(RegisterStudentRequest request);

    void update(UpdateStudentRequest request, @MappingTarget Student student);
}
