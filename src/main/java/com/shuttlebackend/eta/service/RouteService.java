package com.shuttlebackend.eta.service;

import com.shuttlebackend.eta.model.Waypoint;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface RouteService {
    List<Waypoint> getRemainingWaypoints(String vehicleId);
    Optional<Instant> getNextScheduledArrival(String vehicleId);
}

