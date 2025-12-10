package com.shuttlebackend.websocket;

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
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.Instant;

import jakarta.validation.Valid;

@RestController("driverLocationWebSocketController")
@RequiredArgsConstructor
public class DriverLocationWebSocketController {

    private static final Logger logger = LoggerFactory.getLogger(DriverLocationWebSocketController.class);

    private final DriverService driverService;
    private final ShuttleService shuttleService;
    private final DriverSessionService sessionService;
    private final LocationUpdateMapper locationUpdateMapper;
    private final LocationBufferService locationBufferService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/driver/location")
    @Transactional
    public void handleLocation(@Valid ShuttleLocationDto dto, Principal principal) {
        String email = principal == null ? null : principal.getName();

        if (email == null) {
            logger.warn("STOMP location message received without authenticated principal; ignoring.");
            return;
        }

        if (dto.getShuttleId() == null || dto.getLatitude() == null || dto.getLongitude() == null) {
            logger.warn("STOMP location message with missing fields; ignoring.");
            return;
        }

        // shuttleId is Integer in DTO
        Shuttle shuttle = shuttleService.findById(dto.getShuttleId())
                .orElseThrow(() -> new RuntimeException("Shuttle not found"));

        Driver driver = driverService.findDriverByEmail(email)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        var activeSessionOpt = sessionService.findActiveSessionByShuttle(shuttle.getId());
        if (activeSessionOpt.isEmpty()) {
            logger.warn("No active session for shuttle {} — ignoring location", shuttle.getId());
            return;
        }
        var active = activeSessionOpt.get();

        if (!active.getDriver().getId().equals(driver.getId())) {
            logger.warn("Driver {} attempted to send location for shuttle {} but is not session owner", driver.getId(), shuttle.getId());
            return;
        }

        // Build canonical broadcast payload
        LocationBroadcastDto broadcast = new LocationBroadcastDto(
                dto.getShuttleId(),
                dto.getLatitude(),
                dto.getLongitude(),
                dto.getCreatedAt() == null ? Instant.now() : dto.getCreatedAt()
        );

        String topic = "/topic/shuttle/" + dto.getShuttleId() + "/location";
        messagingTemplate.convertAndSend(topic, broadcast);

        // persist as LocationUpdate entity via mapper and buffer it
        LocationUpdateDto updateDto = new LocationUpdateDto();
        updateDto.setShuttleId(dto.getShuttleId());
        updateDto.setLatitude(dto.getLatitude());
        updateDto.setLongitude(dto.getLongitude());
        updateDto.setCreatedAt(broadcast.getTimestamp().toString());

        LocationUpdate lu = locationUpdateMapper.toEntity(updateDto);
        lu.setShuttle(shuttle);
        if (lu.getCreatedAt() == null) lu.setCreatedAt(broadcast.getTimestamp());
        locationBufferService.bufferUpdate(lu);

        // DO NOT directly persist shuttle entity here; persistence is handled in buffer flush
    }
}
