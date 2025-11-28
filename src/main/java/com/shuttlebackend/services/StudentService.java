package com.shuttlebackend.services;

import com.shuttlebackend.repositories.RouteStopRepository;
import com.shuttlebackend.repositories.StudentRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import com.shuttlebackend.dtos.RegisterStudentRequest;
import com.shuttlebackend.entities.Student;
import com.shuttlebackend.entities.User;
import com.shuttlebackend.repositories.SchoolRepository;
import com.shuttlebackend.mappers.StudentMapper;
import java.util.Optional;

@Service
@AllArgsConstructor
public class StudentService {

    private final StudentRepository studentRepo;
    private final UserService userService;
    private final SchoolRepository schoolRepo;
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
        // resolve school by external id (string) first, then fallback to numeric id if the frontend supplied a numeric id string
        var school = schoolRepo.findByExternalId(dto.getSchoolId())
                .or(() -> {
                    try {
                        Integer id = Integer.parseInt(dto.getSchoolId());
                        return schoolRepo.findById(id);
                    } catch (NumberFormatException nfe) {
                        return Optional.empty();
                    }
                })
                .orElseThrow(() -> new RuntimeException("School not found"));
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
