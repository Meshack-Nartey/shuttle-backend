package com.shuttlebackend.repositories;

import com.shuttlebackend.entities.DriverSession;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverSessionRepository extends JpaRepository<DriverSession, Integer> {

    @Modifying
    @Transactional
    @Query("""
        UPDATE DriverSession s
        SET s.endedAt = CURRENT_INSTANT
        WHERE s.driver.id = :driverId
          AND s.endedAt IS NULL
    """)
    void endActiveSessions(@Param("driverId") Integer driverId);

    Optional<DriverSession> findByDriverIdAndEndedAtIsNull(Integer driverId);

    @Query("SELECT s FROM DriverSession s WHERE s.driver.id = :driverId AND s.endedAt IS NULL")
    Optional<DriverSession> findActiveByDriverId(@Param("driverId") Integer driverId);

    @Query("SELECT s FROM DriverSession s WHERE s.shuttle.id = :shuttleId AND s.endedAt IS NULL")
    Optional<DriverSession> findActiveByShuttleId(@Param("shuttleId") Integer shuttleId);

    List<DriverSession> findByRoute_IdAndEndedAtIsNull(Integer routeId);
}
