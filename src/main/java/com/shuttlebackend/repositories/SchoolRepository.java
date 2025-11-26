package com.shuttlebackend.repositories;

import com.shuttlebackend.entities.School;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SchoolRepository extends JpaRepository<School, Integer> {
    Optional<School> findBySchoolName(String schoolName);
}
