package com.shuttlebackend.repositories;

import com.shuttlebackend.entities.TripActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TripActivityRepository extends JpaRepository<TripActivity, Long> {

    List<TripActivity> findByStudent_Id(Integer studentId);


    List<TripActivity> findByShuttle_Id(Integer shuttleId);

    TripActivity findTop1ByStudent_IdOrderByActualTimeDesc(Integer studentId);

}
