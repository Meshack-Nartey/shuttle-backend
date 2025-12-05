package com.shuttlebackend.repositories;

import com.shuttlebackend.entities.RouteStop;
import com.shuttlebackend.entities.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RouteStopRepository extends JpaRepository<RouteStop, Integer> {
    List<RouteStop> findByRoute_IdOrderByStopOrderAsc(Integer routeId);

    List<RouteStop> findByStopNameContainingIgnoreCase(String stopName);

    // Added exact-match and route-scoped queries used by the routing resolver
    List<RouteStop> findByStopNameIgnoreCase(String stopName);

    List<RouteStop> findByStopNameIgnoreCaseAndRoute_Id(String stopName, Integer routeId);

    List<RouteStop> findByRoute_Id(Integer routeId);

    interface StudentRepository extends JpaRepository<Student, Integer> {
        Optional<Student> findByStudentIdNumber(String studentIdNumber);
    }
}
