package com.shuttlebackend.services;

import com.shuttlebackend.entities.Shuttle;
import com.shuttlebackend.repositories.ShuttleRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ShuttleService {

    private final ShuttleRepository repo;

    public Shuttle create(Shuttle s) {
        return repo.save(s);
    }

    public Optional<Shuttle> findById(Integer id) {
        return repo.findById(id);
    }

    public Optional<Shuttle> findByLicense(String plate) {
        return repo.findByLicensePlate(plate);
    }

    public List<Shuttle> findAllBySchool(Integer schoolId) {
        return repo.findAllBySchool_Id(schoolId);
    }

    public Shuttle updateStatus(Shuttle shuttle, String status) {
        shuttle.setStatus(status);
        return repo.save(shuttle);
    }
}
