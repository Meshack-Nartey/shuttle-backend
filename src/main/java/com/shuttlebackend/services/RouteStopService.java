package com.shuttlebackend.services;

import com.shuttlebackend.entities.RouteStop;
import com.shuttlebackend.repositories.RouteStopRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RouteStopService {
    private final RouteStopRepository repo;
    public RouteStopService(RouteStopRepository repo) { this.repo = repo; }
    public RouteStop create(RouteStop s) { return repo.save(s); }
    public List<RouteStop> findByRoute(Integer routeId) {
        return repo.findByRoute_IdOrderByStopOrderAsc(routeId); }
}
