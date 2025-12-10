package com.shuttlebackend.controllers;

import com.shuttlebackend.dtos.TripReminderRequestDto;
import com.shuttlebackend.dtos.EtaResponseDto;
import com.shuttlebackend.dtos.TripActivityDto;
import com.shuttlebackend.entities.TripActivity;
import com.shuttlebackend.entities.Student;
import com.shuttlebackend.entities.RouteStop;
import com.shuttlebackend.entities.Shuttle;
import com.shuttlebackend.services.EtaService;
import com.shuttlebackend.services.TripActivityService;
import com.shuttlebackend.repositories.StudentRepository;
import com.shuttlebackend.repositories.ShuttleRepository;
import com.shuttlebackend.repositories.RouteStopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;

// OpenAPI
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
@Tag(name = "Trips", description = "Trip activity endpoints: create reminders and manage upcoming trips")
public class TripActivityController {

    private final EtaService etaService;
    private final TripActivityService tripActivityService;
    private final StudentRepository studentRepository;
    private final ShuttleRepository shuttleRepository;
    private final RouteStopRepository routeStopRepository;

    @Operation(summary = "Create a trip reminder",
            description = "Creates a trip_activity entry for the student with a scheduled reminder.")
    @PostMapping("/reminders")
    public ResponseEntity<?> createReminder(@RequestBody TripReminderRequestDto req) {
        // validate offset
        int offset = req.getReminderOffsetMinutes() == null ? 0 : req.getReminderOffsetMinutes();
        if (offset != 3 && offset != 5 && offset != 10 && offset != 15) {
            return ResponseEntity.badRequest().body(Map.of("error", "invalid_offset"));
        }

        // compute ETA
        EtaResponseDto eta = etaService.calculateEta(req.getShuttleId(), req.getPickupStopId(), req.getDropoffStopId());
        if (eta == null || eta.getEtaMillis() == null || eta.getEtaMillis() <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "eta_unavailable", "detail", eta == null ? null : eta.getDebug()));
        }

        long etaMillis = eta.getEtaMillis();
        long offsetMillis = offset * 60L * 1000L;
        if (etaMillis < offsetMillis) {
            return ResponseEntity.badRequest().body(Map.of("error", "offset_too_large", "etaMinutes", etaMillis/60000.0, "allowedOffsets", new int[]{3,5,10,15}));
        }

        // look up entities
        Optional<Student> studentOpt = studentRepository.findById(req.getStudentId());
        Optional<Shuttle> shuttleOpt = shuttleRepository.findById(req.getShuttleId());
        Optional<RouteStop> pickupOpt = routeStopRepository.findById(req.getPickupStopId());
        Optional<RouteStop> dropOpt = routeStopRepository.findById(req.getDropoffStopId());

        if (studentOpt.isEmpty() || shuttleOpt.isEmpty() || pickupOpt.isEmpty() || dropOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "entity_not_found"));
        }

        TripActivity ta = new TripActivity();
        ta.setStudent(studentOpt.get());
        ta.setShuttle(shuttleOpt.get());
        ta.setDepartureStop(pickupOpt.get());
        ta.setArrivalStop(dropOpt.get());
        ta.setRouteId(eta.getDirection() != null ? null : null); // preserve existing routeId handling; optional
        ta.setEstimatedTime(Instant.parse(eta.getEtaTimestamp()));
        ta.setStatus("UPCOMING");
        ta.setReminderOffsetMinutes(offset);
        ta.setReminderScheduledAt(Instant.ofEpochMilli(etaMillis - offsetMillis));
        ta.setNotificationSent(false);

        TripActivity created = tripActivityService.create(ta);

        // Map to DTO to avoid lazy-loading issues during JSON serialization
        TripActivityDto dto = new TripActivityDto();
        dto.setTripId(created.getId());
        dto.setStudentId(studentOpt.get().getId().intValue());
        dto.setShuttleId(shuttleOpt.get().getId().intValue());
        dto.setDepartureStopId(pickupOpt.get().getId().intValue());
        dto.setArrivalStopId(dropOpt.get().getId().intValue());
        dto.setRouteId(created.getRouteId());
        dto.setEstimatedTime(created.getEstimatedTime() == null ? null : created.getEstimatedTime().toString());
        dto.setActualTime(created.getActualTime() == null ? null : created.getActualTime().toString());
        dto.setStatus(created.getStatus());
        dto.setReminderOffsetMinutes(created.getReminderOffsetMinutes());
        dto.setReminderScheduledAt(created.getReminderScheduledAt() == null ? null : created.getReminderScheduledAt().toString());
        dto.setNotificationSent(created.getNotificationSent());

        return ResponseEntity.ok(dto);
    }
}
