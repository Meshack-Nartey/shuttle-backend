package com.shuttlebackend.repositories;

import com.shuttlebackend.entities.DriverSession;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverSessionRepository extends JpaRepository<DriverSession, Integer> {

    @Modifying
    @Query("UPDATE DriverSession s SET s.endedAt = CURRENT_TIMESTAMP WHERE s.driver.id = :driverId AND s.endedAt IS NULL")
    void endActiveSessions(Integer driverId);

    Optional<DriverSession> findByDriverIdAndEndedAtIsNull(Integer driverId);

    @Query("SELECT s FROM DriverSession s WHERE s.shuttle.id = :shuttleId AND s.endedAt IS NULL")
    Optional<DriverSession> findActiveByShuttleId(Integer shuttleId);

    // new: find active sessions for a route
    List<DriverSession> findByRoute_IdAndEndedAtIsNull(Integer routeId);
}
