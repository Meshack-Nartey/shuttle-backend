package com.shuttlebackend.services;

import com.shuttlebackend.entities.BlacklistedToken;
import com.shuttlebackend.repositories.BlacklistedTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BlacklistedTokenService {

    private final BlacklistedTokenRepository repo;

    public BlacklistedToken create(String jti, Instant expiresAt) {
        BlacklistedToken t = new BlacklistedToken();
        t.setJti(jti);
        t.setExpiresAt(expiresAt);
        return repo.save(t);
    }

    public Optional<BlacklistedToken> findByJti(String jti) {
        return repo.findByJti(jti);
    }
}

