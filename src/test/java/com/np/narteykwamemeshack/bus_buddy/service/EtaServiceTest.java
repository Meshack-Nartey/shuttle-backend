package com.np.narteykwamemeshack.bus_buddy.service;

import com.np.narteykwamemeshack.bus_buddy.dto.VehicleEtaDto;
import com.np.narteykwamemeshack.bus_buddy.model.Telemetry;
import com.np.narteykwamemeshack.bus_buddy.model.Waypoint;
import com.np.narteykwamemeshack.bus_buddy.repository.TelemetryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class EtaServiceTest {
    private TelemetryRepository telemetryRepository;
    private com.np.narteykwamemeshack.bus_buddy.service.RouteService routeService;
    private SimpMessagingTemplate messagingTemplate;
    private com.np.narteykwamemeshack.bus_buddy.service.EtaService etaService;

    @BeforeEach
    void setUp() {
        telemetryRepository = mock(TelemetryRepository.class);
        routeService = mock(com.np.narteykwamemeshack.bus_buddy.service.RouteService.class);
        messagingTemplate = mock(SimpMessagingTemplate.class);
        etaService = new com.np.narteykwamemeshack.bus_buddy.service.EtaService(telemetryRepository, routeService, messagingTemplate);
    }

    @Test
    void telemetryBasedEta() {
        String vid = "vehicle-1";
        Instant now = Instant.now();
        Telemetry last = new Telemetry(vid, 1.0, 1.0, 10.0, now.minusSeconds(5));
        when(telemetryRepository.findTopByVehicleIdOrderByRecordedAtDesc(vid)).thenReturn(Optional.of(last));
        when(telemetryRepository.findTop5ByVehicleIdOrderByRecordedAtDesc(vid)).thenReturn(Collections.singletonList(last));

        List<Waypoint> waypoints = Arrays.asList(new Waypoint(1.001, 1.001), new Waypoint(1.002,1.002));
        when(routeService.getRemainingWaypoints(vid)).thenReturn(waypoints);

        VehicleEtaDto dto = etaService.computeAndPublishEta(vid);
        assertThat(dto).isNotNull();
        assertThat(dto.getVehicleId()).isEqualTo(vid);
        assertThat(dto.getReason()).isEqualTo("telemetry");
        assertThat(dto.getDistanceMeters()).isGreaterThan(0.0);
        assertThat(dto.getEtaMillis()).isGreaterThan(Instant.now().toEpochMilli());

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        verify(messagingTemplate, times(1)).convertAndSend(topicCaptor.capture(), any());
        assertThat(topicCaptor.getValue()).contains("/topic/vehicles/" + vid + "/eta");
    }

    @Test
    void averageSpeedEstimationUsed() {
        String vid = "vehicle-2";
        Instant now = Instant.now();
        Telemetry last = new Telemetry(vid, 1.0, 1.0, 0.0, now.minusSeconds(1));
        Telemetry prev1 = new Telemetry(vid, 1.0, 1.0, 5.0, now.minusSeconds(4));
        Telemetry prev2 = new Telemetry(vid, 1.0, 1.0, 7.0, now.minusSeconds(8));
        when(telemetryRepository.findTopByVehicleIdOrderByRecordedAtDesc(vid)).thenReturn(Optional.of(last));
        when(telemetryRepository.findTop5ByVehicleIdOrderByRecordedAtDesc(vid)).thenReturn(Arrays.asList(last, prev1, prev2));

        List<Waypoint> waypoints = Arrays.asList(new Waypoint(1.01, 1.01));
        when(routeService.getRemainingWaypoints(vid)).thenReturn(waypoints);

        VehicleEtaDto dto = etaService.computeAndPublishEta(vid);
        assertThat(dto.getReason()).isEqualTo("telemetry");
        assertThat(dto.getDistanceMeters()).isGreaterThan(0.0);
    }

    @Test
    void unavailableWhenNoTelemetryOrRoute() {
        String vid = "vehicle-3";
        when(telemetryRepository.findTopByVehicleIdOrderByRecordedAtDesc(vid)).thenReturn(Optional.empty());
        when(routeService.getRemainingWaypoints(vid)).thenReturn(Collections.emptyList());

        VehicleEtaDto dto = etaService.computeAndPublishEta(vid);
        assertThat(dto.getReason()).isEqualTo("unavailable");
        assertThat(dto.getEtaTimestamp()).isNull();
        verify(messagingTemplate, times(1)).convertAndSend(anyString(), any());
    }
}

