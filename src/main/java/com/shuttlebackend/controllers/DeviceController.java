package com.shuttlebackend.controllers;

import com.shuttlebackend.entities.DeviceToken;
import com.shuttlebackend.entities.Student;
import com.shuttlebackend.repositories.DeviceTokenRepository;
import com.shuttlebackend.repositories.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceTokenRepository deviceRepo;
    private final StudentRepository studentRepo;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, Object> body) {
        Integer studentId = (Integer) body.get("studentId");
        String token = (String) body.get("token");
        String platform = (String) body.get("platform");

        if (studentId == null || token == null || token.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "missing_fields"));
        }

        Optional<Student> sOpt = studentRepo.findById(studentId);
        if (sOpt.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "student_not_found"));

        Student s = sOpt.get();

        DeviceToken existing = deviceRepo.findByToken(token);
        if (existing != null) {
            existing.setIsActive(true);
            existing.setLastSeen(Instant.now());
            deviceRepo.save(existing);
            return ResponseEntity.ok(existing);
        }

        DeviceToken dt = new DeviceToken();
        dt.setStudent(s);
        dt.setToken(token);
        dt.setPlatform(platform);
        dt.setIsActive(true);
        dt.setCreatedAt(Instant.now());
        dt.setLastSeen(Instant.now());

        DeviceToken saved = deviceRepo.save(dt);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/unregister")
    public ResponseEntity<?> unregister(@RequestBody Map<String, Object> body) {
        String token = (String) body.get("token");
        if (token == null) return ResponseEntity.badRequest().body(Map.of("error","missing_token"));
        DeviceToken dt = deviceRepo.findByToken(token);
        if (dt == null) return ResponseEntity.ok(Map.of("ok", true));
        dt.setIsActive(false);
        deviceRepo.save(dt);
        return ResponseEntity.ok(Map.of("ok", true));
    }
}

