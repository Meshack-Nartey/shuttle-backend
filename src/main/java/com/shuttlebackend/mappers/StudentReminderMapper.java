package com.shuttlebackend.mappers;

import com.shuttlebackend.dtos.*;
import com.shuttlebackend.entities.StudentReminder;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface StudentReminderMapper {

    StudentReminderDto toDto(StudentReminder entity);

    StudentReminder toEntity(StudentReminderDto dto);

    StudentReminder toEntity(RegisterStudentReminderRequest request);

    void update(UpdateStudentReminderRequest request, @MappingTarget StudentReminder entity);
}
