package com.shuttlebackend.repositories;

import com.shuttlebackend.entities.StudentReminder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StudentReminderRepository extends JpaRepository<StudentReminder, Integer> {
    List<StudentReminder> findByStudent_Id(Integer studentId);
}
