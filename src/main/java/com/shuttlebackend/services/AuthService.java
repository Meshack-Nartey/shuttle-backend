package com.shuttlebackend.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import com.shuttlebackend.security.JwtHelper;
import com.shuttlebackend.entities.User;
import com.shuttlebackend.services.RefreshTokenService;

import java.time.Instant;

@Service
@AllArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtHelper jwtHelper;
    private final RefreshTokenService refreshTokenService;


    public String issueToken(User user) {
        return jwtHelper.generateAccessToken(user.getEmail());
    }

    public String issueAccessToken(User user) {
        return jwtHelper.generateAccessToken(user.getEmail());
    }

    public String issueRefreshToken(User user) {
        String token = jwtHelper.generateRefreshToken(user.getEmail());
        String jti = jwtHelper.getJti(token);
        long expMs = jwtHelper.getExpirationMillis(token);
        Instant expiresAt = Instant.ofEpochMilli(expMs);
        // persist refresh token
        refreshTokenService.create(jti, user.getEmail(), expiresAt);
        return token;
    }
}
