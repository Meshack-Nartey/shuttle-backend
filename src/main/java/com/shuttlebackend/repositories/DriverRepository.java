package com.shuttlebackend.repositories;

import com.shuttlebackend.entities.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DriverRepository extends JpaRepository<Driver, Integer> {

    // find driver by the nested user's email (property navigation)
    Optional<Driver> findByUser_Email(String email);
}
