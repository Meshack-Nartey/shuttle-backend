package com.shuttlebackend.controllers;

import com.shuttlebackend.dtos.*;
import com.shuttlebackend.entities.User;
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

import java.util.Arrays;

@RestController
@AllArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final UserService userService;
    private final StudentService studentService;
    private final DriverService driverService;
    private final ShuttleService shuttleService;
    private final DriverSessionService driverSessionService;
    private final AuthService authService;

    private final StudentMapper studentMapper;
    private final DriverMapper driverMapper;
    private final UserMapper userMapper;

    private final JwtHelper jwtHelper;


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

    // LOGIN
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> login(@RequestBody JwtRequest request, HttpServletResponse response) {

        String principalEmail = request.getEmail();
        if (principalEmail == null || principalEmail.isBlank())
            throw new RuntimeException("Email is required for login");

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

        // Set refresh token cookie
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

        JwtResponse resp = new JwtResponse(access, userMapper.toDto(user));
        return ResponseEntity.ok(new ApiResponse<>(true, resp));
    }

    // REFRESH TOKEN (STATELESS)
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<JwtResponse>> refresh(@RequestBody(required = false) RefreshRequest req,
                                                            HttpServletRequest request,
                                                            HttpServletResponse response) {
        String token = null;

        // check body first
        if (req != null && req.getRefreshToken() != null && !req.getRefreshToken().isBlank()) {
            token = req.getRefreshToken();
        }

        // else check cookies
        if (token == null && request.getCookies() != null) {
            token = Arrays.stream(request.getCookies())
                    .filter(c -> "refreshToken".equals(c.getName()))
                    .map(c -> c.getValue())
                    .findFirst()
                    .orElse(null);
        }

        if (token == null || token.isBlank()) {
            throw new RuntimeException("refreshToken is required");
        }

        if (!jwtHelper.validateToken(token) || !"refresh".equals(jwtHelper.getType(token))) {
            throw new RuntimeException("Invalid refresh token");
        }

        String email = jwtHelper.getSubject(token);
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // issue new tokens
        String newAccess = authService.issueAccessToken(user);
        String newRefresh = authService.issueRefreshToken(user);

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

    // LOGOUT - stateless: just delete cookie (no blacklist)
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<?>> logout(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            HttpServletResponse response) {

        // clear cookie
        ResponseCookie clearCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, clearCookie.toString());

        return ResponseEntity.ok(new ApiResponse<>(true, "Logged out"));
    }

    @PostMapping("/logout/all")
    public ResponseEntity<ApiResponse<?>> logoutAll(HttpServletResponse response) {

        // Stateless system: cannot revoke all refresh tokens.
        // Only clear cookie and tell client to re-login.

        ResponseCookie clearCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, clearCookie.toString());

        return ResponseEntity.ok(new ApiResponse<>(true, "All sessions cleared"));
    }
}
