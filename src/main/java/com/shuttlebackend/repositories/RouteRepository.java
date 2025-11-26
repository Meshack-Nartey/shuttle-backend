package com.shuttlebackend.repositories;

import com.shuttlebackend.entities.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RouteRepository extends JpaRepository<Route, Integer> {
    List<Route> findBySchool_Id(Integer schoolId);
}
