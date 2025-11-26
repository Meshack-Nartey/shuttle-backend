package com.shuttlebackend.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import com.shuttlebackend.repositories.TripActivityRepository;
import com.shuttlebackend.entities.TripActivity;
import java.util.List;

@Service
@AllArgsConstructor
public class TripActivityService {
    private final TripActivityRepository repo;

    public TripActivity create(TripActivity t) { return repo.save(t); }
    public List<TripActivity> findByStudent(Integer studentId) {
        return repo.findByStudent_Id(studentId); }
    public List<TripActivity> findByShuttle(Integer shuttleId) { return repo.findByShuttle_Id(shuttleId); }
}
