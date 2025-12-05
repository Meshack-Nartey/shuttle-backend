package com.shuttlebackend.repositories;

import com.shuttlebackend.entities.School;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SchoolRepository extends JpaRepository<School, Integer> {

    // direct match
    Optional<School> findBySchoolName(String schoolName);

    // case-insensitive + trimmed
    @Query("SELECT s FROM School s WHERE LOWER(TRIM(s.schoolName)) = LOWER(TRIM(:schoolName))")
    Optional<School> findBySchoolNameIgnoreCaseTrim(@Param("schoolName") String schoolName);
}
