package com.shuttlebackend.repositories;

import com.shuttlebackend.entities.LocationUpdate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LocationUpdateRepository extends JpaRepository<LocationUpdate, Long> {

    List<LocationUpdate> findTop20ByShuttle_IdOrderByCreatedAtDesc(Integer shuttleId);
}
