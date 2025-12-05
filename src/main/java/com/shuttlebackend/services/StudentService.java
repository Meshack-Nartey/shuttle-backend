package com.shuttlebackend.services;

import com.shuttlebackend.repositories.RouteStopRepository;
import com.shuttlebackend.repositories.StudentRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import com.shuttlebackend.dtos.RegisterStudentRequest;
import com.shuttlebackend.entities.Student;
import com.shuttlebackend.entities.User;
import com.shuttlebackend.entities.School;
import com.shuttlebackend.services.SchoolService;
import com.shuttlebackend.mappers.StudentMapper;
import java.util.Optional;

@Service
@AllArgsConstructor
public class StudentService {

    private final StudentRepository studentRepo;
    private final UserService userService;
    private final SchoolService schoolService;
    private final StudentMapper studentMapper;


    public Student signupStudent(RegisterStudentRequest dto) {
        if (studentRepo.findByStudentIdNumber(dto.getStudentIdNumber()).isPresent()) {
            throw new RuntimeException("Student ID already exists");
        }
        if (userService.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("Email already in use");
        }
        // create user account
        User user = userService.createUser(dto.getEmail(), dto.getPassword(), "ROLE_STUDENT");
        // Resolve school by display name provided by frontend (case-insensitive, trimmed)
        var school = schoolService.findByNameIgnoreCaseTrim(dto.getSchoolName())
                .orElseThrow(() -> new RuntimeException("Invalid school selected"));
        Student student = studentMapper.toEntity(dto);
        student.setUser(user);
        if (dto.getUsername() != null && !dto.getUsername().isBlank()) {
            student.setUsername(dto.getUsername());
        }
        student.setSchool(school);
        return studentRepo.save(student);
    }

    public Optional<Student> findById(Integer id) {
        return studentRepo.findById(id);
    }
}
