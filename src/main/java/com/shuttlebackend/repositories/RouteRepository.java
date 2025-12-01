package com.shuttlebackend.repositories;

import com.shuttlebackend.entities.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface RouteRepository extends JpaRepository<Route, Integer> {
    List<Route> findBySchool_Id(Integer schoolId);

    @Query("SELECT DISTINCT r FROM Route r JOIN r.routeStops rs1 JOIN r.routeStops rs2 WHERE rs1.id = :pickupStopId AND rs2.id = :dropoffStopId")
    List<Route> findRoutesContainingBothStops(@Param("pickupStopId") Integer pickupStopId, @Param("dropoffStopId") Integer dropoffStopId);
}
