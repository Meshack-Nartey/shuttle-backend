package com.shuttlebackend.services;

import org.springframework.stereotype.Service;
import com.shuttlebackend.repositories.LocationUpdateRepository;
import com.shuttlebackend.entities.LocationUpdate;
import java.util.List;

@Service
public class LocationUpdateService {
    private final LocationUpdateRepository repo;
    public LocationUpdateService(LocationUpdateRepository repo) { this.repo = repo; }

    public LocationUpdate save(LocationUpdate lu) { return repo.save(lu); }

    public List<LocationUpdate> latestForShuttle(Integer shuttleId) {
        return repo.findTop20ByShuttle_IdOrderByCreatedAtDesc(shuttleId);
    }
}
