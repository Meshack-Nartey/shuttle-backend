package com.shuttlebackend.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shuttlebackend.dtos.LocationUpdateDto;
import com.shuttlebackend.dtos.ShuttleLocationDto;
import com.shuttlebackend.entities.LocationUpdate;
import com.shuttlebackend.entities.Shuttle;
import com.shuttlebackend.entities.Driver;
import com.shuttlebackend.services.DriverService;
import com.shuttlebackend.services.ShuttleService;
import com.shuttlebackend.services.DriverSessionService;
import com.shuttlebackend.websocket.buffer.LocationBufferService;
import com.shuttlebackend.mappers.LocationUpdateMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.Instant;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class LocationWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper mapper;
    private final DriverService driverService;
    private final ShuttleService shuttleService;
    private final DriverSessionService sessionService;
    private final LocationUpdateMapper locationUpdateMapper;
    private final LocationBufferService locationBufferService;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Map<String, Object> attribs = session.getAttributes();
        String email = (String) attribs.get("email");
        if (email == null) {
            session.close();
            return;
        }

        LocationUpdateDto dto = mapper.readValue(message.getPayload(), LocationUpdateDto.class);

        // basic validation: shuttle exists
        Shuttle shuttle = shuttleService.findById(dto.getShuttleId()).orElseThrow(() -> new RuntimeException("Shuttle not found"));

        // ensure driver is active session owner for this shuttle
        Driver driver = driverService.findDriverByEmail(email).orElseThrow(() -> new RuntimeException("Driver not found"));
        var active = sessionService.findActiveSessionByShuttle(shuttle.getId()).orElseThrow(() -> new RuntimeException("No active session for this shuttle"));
        if (!active.getDriver().getId().equals(driver.getId())) {
            // ignore or close
            session.sendMessage(new TextMessage(mapper.writeValueAsString(Map.of("error", "not_active"))));
            return;
        }

        // Build ShuttleLocationDto
        ShuttleLocationDto loc = new ShuttleLocationDto();
        loc.setShuttleId(dto.getShuttleId());
        loc.setLatitude(dto.getLatitude());
        loc.setLongitude(dto.getLongitude());
        loc.setCreatedAt(dto.getCreatedAt() == null ? Instant.now() : Instant.parse(dto.getCreatedAt()));

        // Broadcast immediately over STOMP topic to connected students
        String topic = "/topic/shuttle/" + loc.getShuttleId() + "/location";
        messagingTemplate.convertAndSend(topic, loc);

        // Also buffer/persist the update for later batch-save
        LocationUpdate lu = locationUpdateMapper.toEntity(dto);
        lu.setShuttle(shuttle);
        if (lu.getCreatedAt() == null) lu.setCreatedAt(loc.getCreatedAt());
        locationBufferService.bufferUpdate(lu);
    }
}
