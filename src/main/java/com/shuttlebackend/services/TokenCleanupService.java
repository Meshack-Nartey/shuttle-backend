package com.shuttlebackend.services;

import com.shuttlebackend.repositories.RefreshTokenRepository;
import com.shuttlebackend.repositories.BlacklistedTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TokenCleanupService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final BlacklistedTokenRepository blacklistedTokenRepository;

    // run every hour, first run after 1 minute
    @Scheduled(initialDelayString = "60000", fixedDelayString = "3600000")
    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteExpired();
        blacklistedTokenRepository.deleteExpired();
    }
}

