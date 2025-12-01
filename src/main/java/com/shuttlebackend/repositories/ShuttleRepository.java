package com.shuttlebackend.repositories;

import com.shuttlebackend.entities.Shuttle;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ShuttleRepository extends JpaRepository<Shuttle, Integer> {

    Optional<Shuttle> findByLicensePlate(String licensePlate);

    List<Shuttle> findAllBySchool_Id(Integer schoolId);

    List<Shuttle> findAllByStatus(String status);

    // fast cardinality check to avoid pulling full rows when none are active
    long countByStatus(String status);
}
