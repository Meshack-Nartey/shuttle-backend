package com.shuttlebackend.controllers;

import com.shuttlebackend.dtos.LocationBroadcastDto;
import com.shuttlebackend.dtos.ShuttleLocationDto;
import com.shuttlebackend.dtos.LocationUpdateDto;
import com.shuttlebackend.entities.LocationUpdate;
import com.shuttlebackend.entities.Shuttle;
import com.shuttlebackend.entities.Driver;
import com.shuttlebackend.mappers.LocationUpdateMapper;
import com.shuttlebackend.services.DriverService;
import com.shuttlebackend.services.ShuttleService;
import com.shuttlebackend.services.DriverSessionService;
import com.shuttlebackend.websocket.buffer.LocationBufferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.Instant;

@RestController
@RequiredArgsConstructor
public class DriverLocationController {

    private static final Logger logger = LoggerFactory.getLogger(DriverLocationController.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final DriverService driverService;
    private final ShuttleService shuttleService;
    private final DriverSessionService sessionService;
    private final LocationUpdateMapper locationUpdateMapper;
    private final LocationBufferService locationBufferService;

    @PostMapping("/api/driver/location")
    public void handleLocation(@RequestBody @Valid ShuttleLocationDto dto, Principal principal) {
        String email = principal == null ? null : principal.getName();
        if (email == null) {
            logger.warn("REST location update without authenticated principal; ignoring.");
            return;
        }

        if (dto.getShuttleId() == null || dto.getLatitude() == null || dto.getLongitude() == null) {
            logger.warn("REST location update with missing fields; ignoring.");
            return;
        }

        Shuttle shuttle = shuttleService.findById(dto.getShuttleId()).orElseThrow(() -> new RuntimeException("Shuttle not found"));
        Driver driver = driverService.findDriverByEmail(email).orElseThrow(() -> new RuntimeException("Driver not found"));

        var activeSessionOpt = sessionService.findActiveSessionByShuttle(shuttle.getId());
        if (activeSessionOpt.isEmpty()) {
            logger.warn("REST: No active session for shuttle {} — ignoring location", shuttle.getId());
            return;
        }

        if (!activeSessionOpt.get().getDriver().getId().equals(driver.getId())) {
            logger.warn("REST: Driver {} attempted to send location for shuttle {} but is not session owner", driver.getId(), shuttle.getId());
            return;
        }

        LocationBroadcastDto broadcast = new LocationBroadcastDto(
                dto.getShuttleId(),
                dto.getLatitude(),
                dto.getLongitude(),
                dto.getCreatedAt() == null ? Instant.now() : dto.getCreatedAt()
        );

        String topic = "/topic/shuttle/" + dto.getShuttleId() + "/location";
        messagingTemplate.convertAndSend(topic, broadcast);

        // persist via buffer
        LocationUpdateDto updateDto = new LocationUpdateDto();
        updateDto.setShuttleId(dto.getShuttleId());
        updateDto.setLatitude(dto.getLatitude());
        updateDto.setLongitude(dto.getLongitude());
        updateDto.setCreatedAt(broadcast.getTimestamp().toString());

        LocationUpdate lu = locationUpdateMapper.toEntity(updateDto);
        lu.setShuttle(shuttle);
        if (lu.getCreatedAt() == null) lu.setCreatedAt(broadcast.getTimestamp());
        locationBufferService.bufferUpdate(lu);
    }
}
