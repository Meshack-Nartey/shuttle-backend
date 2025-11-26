package com.shuttlebackend.services;

import com.shuttlebackend.entities.RefreshToken;
import com.shuttlebackend.repositories.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository repo;

    public RefreshToken create(String jti, String userEmail, Instant expiresAt) {
        RefreshToken t = new RefreshToken();
        t.setJti(jti);
        t.setUserEmail(userEmail);
        t.setExpiresAt(expiresAt);
        t.setRevoked(false);
        return repo.save(t);
    }

    public Optional<RefreshToken> findByJti(String jti) {
        return repo.findByJti(jti);
    }

    public void revokeByJti(String jti) {
        repo.findByJti(jti).ifPresent(t -> {
            t.setRevoked(true);
            repo.save(t);
        });
    }

    public void revokeAllForUser(String email) {
        repo.findByUserEmail(email).forEach(t -> {
            t.setRevoked(true);
            repo.save(t);
        });
    }
}

