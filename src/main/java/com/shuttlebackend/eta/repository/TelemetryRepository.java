package com.shuttlebackend.eta.repository;

import com.shuttlebackend.eta.model.Telemetry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TelemetryRepository extends JpaRepository<Telemetry, Long> {
    Optional<Telemetry> findTopByVehicleIdOrderByRecordedAtDesc(String vehicleId);
    List<Telemetry> findTop5ByVehicleIdOrderByRecordedAtDesc(String vehicleId);
}

