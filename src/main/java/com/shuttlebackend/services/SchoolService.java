package com.shuttlebackend.services;

import com.shuttlebackend.entities.School;
import com.shuttlebackend.repositories.SchoolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SchoolService {

    private final SchoolRepository repo;

    public Optional<School> findById(Integer id) {
        return repo.findById(id);
    }

    public School save(School school) {
        return repo.save(school);
    }
}
