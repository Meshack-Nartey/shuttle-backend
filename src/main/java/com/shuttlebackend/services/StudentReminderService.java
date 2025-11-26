package com.shuttlebackend.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import com.shuttlebackend.repositories.StudentReminderRepository;
import com.shuttlebackend.entities.StudentReminder;
import java.util.List;

@Service
@AllArgsConstructor
public class StudentReminderService {
    private final StudentReminderRepository repo;

    public StudentReminder create(StudentReminder r) { return repo.save(r); }
    public List<StudentReminder> findByStudent(Integer studentId) {
        return repo.findByStudent_Id(studentId); }
}
