package com.shuttlebackend.repositories;

import com.shuttlebackend.entities.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Integer> {
    Optional<Student> findByStudentIdNumber(String studentIdNumber);
}
