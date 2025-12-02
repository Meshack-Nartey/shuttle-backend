package com.shuttlebackend.services;

import com.shuttlebackend.dtos.EtaResponseDto;
import com.shuttlebackend.entities.DriverSession;
import com.shuttlebackend.entities.LocationUpdate;
import com.shuttlebackend.entities.Route;
import com.shuttlebackend.entities.RouteStop;
import com.shuttlebackend.repositories.DriverSessionRepository;
import com.shuttlebackend.repositories.LocationUpdateRepository;
import com.shuttlebackend.repositories.RouteRepository;
import com.shuttlebackend.repositories.RouteStopRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EtaServiceUnitTest {

    @Mock
    DriverSessionRepository sessionRepository;

    @Mock
    LocationUpdateRepository locationUpdateRepository;

    @Mock
    RouteRepository routeRepository;

    @Mock
    RouteStopRepository routeStopRepository;

    @Mock
    SpeedEstimatorService speedEstimator;

    @Mock
    DirectionDetectorService directionDetector;

    @Mock
    SimpMessagingTemplate messagingTemplate;

    // use a real polyline service
    PolylineDistanceService polyService;

    EtaService etaService;

    @BeforeEach
    void setUp() {
        polyService = new PolylineDistanceService();
        etaService = new EtaService(sessionRepository, locationUpdateRepository, routeStopRepository, polyService, speedEstimator, directionDetector, messagingTemplate);
    }

    @Test
    void calculateEta_basicScenario() {
        Integer shuttleId = 42;
        Integer pickupId = 1;
        Integer dropoffId = 2;

        // build route with simple polyline
        Route route = new Route();
        route.setId(100);
        List<List<Double>> polyTypedF = List.of(List.of(0.0, 0.0), List.of(0.0, 0.001), List.of(0.0, 0.002));
        route.setPolylineForward(polyTypedF);
        route.setPolylineBackward(List.of(List.of(0.0, 0.002), List.of(0.0, 0.001), List.of(0.0, 0.0)));

        DriverSession session = new DriverSession();
        session.setId(7);
        session.setRoute(route);

        // latest location near first point
        LocationUpdate lu = new LocationUpdate();
        lu.setLatitude(BigDecimal.valueOf(0.0));
        lu.setLongitude(BigDecimal.valueOf(0.0001));
        lu.setCreatedAt(Instant.now());

        RouteStop pickup = new RouteStop();
        pickup.setId(pickupId);
        pickup.setLatitude(BigDecimal.valueOf(0.0));
        pickup.setLongitude(BigDecimal.valueOf(0.001));

        RouteStop dropoff = new RouteStop();
        dropoff.setId(dropoffId);
        dropoff.setLatitude(BigDecimal.valueOf(0.0));
        dropoff.setLongitude(BigDecimal.valueOf(0.002));

        // mocks
        when(sessionRepository.findActiveByShuttleId(shuttleId)).thenReturn(Optional.of(session));
        when(locationUpdateRepository.findTop1ByShuttle_IdOrderByCreatedAtDesc(shuttleId)).thenReturn(Optional.of(lu));
        when(routeStopRepository.findById(pickupId)).thenReturn(Optional.of(pickup));
        when(routeStopRepository.findById(dropoffId)).thenReturn(Optional.of(dropoff));
        when(speedEstimator.estimateSpeedKph(shuttleId)).thenReturn(20.0);
        when(directionDetector.detectDirection(shuttleId, route.getId(), lu.getLatitude().doubleValue(), lu.getLongitude().doubleValue(), route.getPolylineForward(), route.getPolylineBackward()))
                .thenReturn(DirectionDetectorService.Direction.FORWARD);

        EtaResponseDto resp = etaService.calculateEta(shuttleId, pickupId, dropoffId);

        assertThat(resp).isNotNull();
        assertThat(resp.getShuttleId()).isEqualTo(shuttleId);
        assertThat(resp.getTotalDistance()).isGreaterThan(0.0);
        assertThat(resp.getEtaMillis()).isGreaterThan(0L);
        assertThat(resp.getDirection()).isEqualTo("forward");
        assertThat(resp.getSpeedKph()).isEqualTo(20.0);
    }
}
