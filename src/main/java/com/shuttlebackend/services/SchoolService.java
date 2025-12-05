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

    // Resolve school by display name (case-insensitive, trimming whitespace)
    public Optional<School> findByNameIgnoreCaseTrim(String name) {
        if (name == null) return Optional.empty();
        return repo.findBySchoolNameIgnoreCaseTrim(name);
    }
}
