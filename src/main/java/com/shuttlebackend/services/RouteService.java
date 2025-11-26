package com.shuttlebackend.services;

import org.springframework.stereotype.Service;
import com.shuttlebackend.repositories.RouteRepository;
import com.shuttlebackend.repositories.RouteStopRepository;
import com.shuttlebackend.entities.Route;
import com.shuttlebackend.entities.RouteStop;
import java.util.List;
import java.util.Optional;

@Service
public class RouteService {
    private final RouteRepository repo;
    public RouteService(RouteRepository repo) { this.repo = repo; }
    public Route create(Route r) { return repo.save(r); }
    public List<Route> findBySchool(Integer schoolId) { return repo.findBySchool_Id(schoolId); }
    public Optional<Route> findById(Integer id) { return repo.findById(id); }
}
