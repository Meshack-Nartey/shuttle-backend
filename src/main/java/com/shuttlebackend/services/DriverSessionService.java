package com.shuttlebackend.services;

import com.shuttlebackend.entities.Driver;
import com.shuttlebackend.entities.Shuttle;
import com.shuttlebackend.entities.Route;
import com.shuttlebackend.entities.DriverSession;
import com.shuttlebackend.exceptions.ActiveSessionExistsException;
import com.shuttlebackend.repositories.DriverSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DriverSessionService {

    private final DriverSessionRepository repo;
    private final ShuttleService shuttleService;

    @Transactional
    public DriverSession startSession(Driver driver, Shuttle shuttle) {
        return startSession(driver, shuttle, null);
    }

    @Transactional
    public DriverSession startSession(Driver driver, Shuttle shuttle, Route route) {

        // ensure shuttle not in use by another active session
        repo.findActiveByShuttleId(shuttle.getId()).ifPresent(active -> {
            if (!active.getDriver().getId().equals(driver.getId())) {
                throw new IllegalStateException("Shuttle already in use by another driver");
            }
            // if the active session belongs to the same driver, we will allow the request to proceed
            // (do not auto-end the existing session here)
        });

        // Check if driver already has an active session — if so, reject start request
        boolean hasActive = repo.findActiveByDriverId(driver.getId()).isPresent();
        if (hasActive) {
            throw new ActiveSessionExistsException("You already have an active session. Please end your current session before starting a new one.");
        }

        // create a new session only if no active session exists
        DriverSession session = new DriverSession();
        session.setDriver(driver);
        session.setShuttle(shuttle);
        session.setRoute(route);
        session.setStartedAt(Instant.now());

        // set shuttle as active
        shuttleService.updateStatus(shuttle, "Active");

        return repo.save(session);
    }

    // helper to find the current active session for a driver
    public Optional<DriverSession> findActiveSessionForDriver(Integer driverId) {
        return repo.findByDriverIdAndEndedAtIsNull(driverId);
    }

    // helper to find active session by shuttle id
    public Optional<DriverSession> findActiveSessionByShuttle(Integer shuttleId) {
        return repo.findActiveByShuttleId(shuttleId);
    }

    // End a specific session by id
    @Transactional
    public DriverSession endSessionById(Integer sessionId) {
        DriverSession session = repo.findById(sessionId).orElseThrow(() -> new RuntimeException("Session not found"));
        if (session.getEndedAt() != null) return session; // already ended
        session.setEndedAt(Instant.now());
        // set shuttle to available
        shuttleService.updateStatus(session.getShuttle(), "Available");
        return repo.save(session);
    }

    // End the active session for a given driver
    @Transactional
    public DriverSession endActiveSessionForDriver(Integer driverId) {
        DriverSession active = repo.findByDriverIdAndEndedAtIsNull(driverId).orElseThrow(() -> new RuntimeException("No active session for driver"));
        active.setEndedAt(Instant.now());
        shuttleService.updateStatus(active.getShuttle(), "Available");
        return repo.save(active);
    }
}
