package com.shuttlebackend.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import com.shuttlebackend.security.JwtHelper;
import com.shuttlebackend.entities.User;

@Service
@AllArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtHelper jwtHelper;

    // Issue short-lived access token
    public String issueToken(User user) {
        return jwtHelper.generateAccessToken(user.getEmail());
    }

    public String issueAccessToken(User user) {
        return jwtHelper.generateAccessToken(user.getEmail());
    }

    // Issue refresh token WITHOUT SAVING IT ANYWHERE
    public String issueRefreshToken(User user) {
        return jwtHelper.generateRefreshToken(user.getEmail());
    }
}
