package com.shuttlebackend.repositories;

import com.shuttlebackend.entities.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {
    List<DeviceToken> findByStudent_IdAndIsActiveTrue(Integer studentId);
    DeviceToken findByToken(String token);
}

