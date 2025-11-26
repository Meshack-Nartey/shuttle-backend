package com.shuttlebackend.controllers;

import com.shuttlebackend.dtos.*;
import com.shuttlebackend.dtos.StudentDto;
import com.shuttlebackend.dtos.DriverDto;
import com.shuttlebackend.dtos.UserDto;
import com.shuttlebackend.entities.User;
import com.shuttlebackend.entities.RefreshToken;
import com.shuttlebackend.services.*;
import com.shuttlebackend.utils.ApiResponse;
import com.shuttlebackend.mappers.StudentMapper;
import com.shuttlebackend.mappers.DriverMapper;
import com.shuttlebackend.mappers.UserMapper;
import com.shuttlebackend.security.JwtHelper;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;

@RestController
@AllArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final UserService userService;
    private final StudentService studentService;
    private final DriverService driverService;
    private final ShuttleService shuttleService;              // kept for later use
    private final DriverSessionService driverSessionService;  // kept for later use
    private final AuthService authService;

    private final StudentMapper studentMapper;
    private final DriverMapper driverMapper;
    private final UserMapper userMapper;

    private final JwtHelper jwtHelper;
    private final RefreshTokenService refreshTokenService;
    private final BlacklistedTokenService blacklistedTokenService;

    // STUDENT SIGNUP
    @PostMapping("/signup/student")
    public ResponseEntity<ApiResponse<?>> signupStudent(@RequestBody RegisterStudentRequest req) {
        var student = studentService.signupStudent(req);
        StudentDto dto = studentMapper.toDto(student);
        return ResponseEntity.status(201).body(new ApiResponse<>(true, dto));
    }

    // DRIVER SIGNUP
    @PostMapping("/signup/driver")
    public ResponseEntity<ApiResponse<?>> signupDriver(@RequestBody RegisterDriverRequest req) {
        var driver = driverService.registerDriver(req);
        DriverDto dto = driverMapper.toDto(driver);
        return ResponseEntity.status(201).body(new ApiResponse<>(true, dto));
    }

    // LOGIN (email + password)
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> login(@RequestBody JwtRequest request, HttpServletResponse response) {

        String principalEmail = request.getEmail();
        if (principalEmail == null || principalEmail.isBlank()) {
            throw new RuntimeException("Email is required for login");
        }

        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(principalEmail, request.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new RuntimeException("Invalid email or password");
        }

        User user = userService.findByEmail(principalEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String access = authService.issueAccessToken(user);
        String refresh = authService.issueRefreshToken(user);

        // set refresh token in HttpOnly cookie (not returned in JSON)
        long expiresMs = jwtHelper.getExpirationMillis(refresh);
        long maxAgeSeconds = Math.max(0, (expiresMs - System.currentTimeMillis()) / 1000);

        ResponseCookie cookie = ResponseCookie.from("refreshToken", refresh)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(maxAgeSeconds)
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        UserDto userDto = userMapper.toDto(user);

        JwtResponse resp = new JwtResponse(access, userDto);

        return ResponseEntity.status(200)
                .body(new ApiResponse<>(true, resp));
    }

    // REFRESH TOKENS
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<JwtResponse>> refresh(@RequestBody(required = false) RefreshRequest req, HttpServletRequest request, HttpServletResponse response) {
        String token = null;
        if (req != null && req.getRefreshToken() != null && !req.getRefreshToken().isBlank()) {
            token = req.getRefreshToken();
        }

        // if not provided in body, try cookie
        if (token == null || token.isBlank()) {
            if (request.getCookies() != null) {
                token = Arrays.stream(request.getCookies())
                        .filter(c -> "refreshToken".equals(c.getName()))
                        .map(c -> c.getValue())
                        .findFirst()
                        .orElse(null);
            }
        }

        if (token == null || token.isBlank()) {
            throw new RuntimeException("refreshToken is required");
        }

        if (!jwtHelper.validateToken(token) || !"refresh".equals(jwtHelper.getType(token))) {
            throw new RuntimeException("Invalid refresh token");
        }

        String jti = jwtHelper.getJti(token);
        RefreshToken stored = refreshTokenService.findByJti(jti).orElseThrow(() -> new RuntimeException("Refresh token not found"));
        if (stored.getRevoked() || stored.getExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("Refresh token revoked or expired");
        }

        String email = stored.getUserEmail();
        User user = userService.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        // rotate: revoke old and create new
        refreshTokenService.revokeByJti(jti);

        String newAccess = authService.issueAccessToken(user);
        String newRefresh = authService.issueRefreshToken(user);

        // set new refresh token cookie
        long expiresMs = jwtHelper.getExpirationMillis(newRefresh);
        long maxAgeSeconds = Math.max(0, (expiresMs - System.currentTimeMillis()) / 1000);

        ResponseCookie cookie = ResponseCookie.from("refreshToken", newRefresh)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(maxAgeSeconds)
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        JwtResponse resp = new JwtResponse(newAccess, userMapper.toDto(user));
        return ResponseEntity.ok(new ApiResponse<>(true, resp));
    }

    // Revoke a single refresh token (logout single session)
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<?>> logout(@RequestBody(required = false) RefreshRequest req, @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader, HttpServletRequest request, HttpServletResponse response) {
        String token = null;
        if (req != null && req.getRefreshToken() != null && !req.getRefreshToken().isBlank()) {
            token = req.getRefreshToken();
        }

        // if not provided in body, try cookie
        if (token == null || token.isBlank()) {
            if (request.getCookies() != null) {
                token = Arrays.stream(request.getCookies())
                        .filter(c -> "refreshToken".equals(c.getName()))
                        .map(c -> c.getValue())
                        .findFirst()
                        .orElse(null);
            }
        }

        if (token == null || token.isBlank()) {
            throw new RuntimeException("refreshToken is required");
        }

        if (!jwtHelper.validateToken(token)) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Invalid token"));
        }

        String jti = jwtHelper.getJti(token);
        refreshTokenService.revokeByJti(jti);

        // if access token passed in Authorization header, blacklist it
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String access = authHeader.substring(7);
            if (jwtHelper.validateToken(access) && "access".equals(jwtHelper.getType(access))) {
                String accessJti = jwtHelper.getJti(access);
                long expMs = jwtHelper.getExpirationMillis(access);
                blacklistedTokenService.create(accessJti, Instant.ofEpochMilli(expMs));
            }
        }

        // clear refresh token cookie
        ResponseCookie clearCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, clearCookie.toString());

        return ResponseEntity.ok(new ApiResponse<>(true, "Token revoked"));
    }

    // Revoke all refresh tokens for the current authenticated user
    @PostMapping("/logout/all")
    public ResponseEntity<ApiResponse<?>> logoutAll(HttpServletResponse response) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        refreshTokenService.revokeAllForUser(email);
        // we cannot reliably find all access jtis without storing them; advise that access tokens will expire soon

        // clear refresh cookie on client
        ResponseCookie clearCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, clearCookie.toString());

        return ResponseEntity.ok(new ApiResponse<>(true, "All tokens revoked"));
    }
}
