package com.shuttlebackend.services;

import com.shuttlebackend.entities.Route;
import com.shuttlebackend.entities.RouteStop;
import com.shuttlebackend.repositories.RouteStopRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class RoutingResolverServiceTest {
    private RouteStopRepository repo;
    private RoutingResolverService service;

    @BeforeEach
    void setup() {
        repo = Mockito.mock(RouteStopRepository.class);
        service = new RoutingResolverService(repo);
    }

    private RouteStop makeStop(int id, int routeId, String name, int order, String direction) {
        RouteStop rs = new RouteStop();
        rs.setId(id);
        Route r = new Route();
        r.setId(routeId);
        rs.setRoute(r);
        rs.setStopName(name);
        rs.setLatitude(BigDecimal.ZERO);
        rs.setLongitude(BigDecimal.ZERO);
        rs.setStopOrder(order);
        rs.setDirection(direction);
        return rs;
    }

    @Test
    void returnsErrorWhenDifferentRoutes() {
        RouteStop p1 = makeStop(1, 10, "Brunei Bus Stop", 2, "FORWARD");
        RouteStop d1 = makeStop(2, 11, "GRASAG / Trinity Bus Stop", 5, "FORWARD");

        when(repo.findByStopNameIgnoreCase("Brunei Bus Stop")).thenReturn(List.of(p1));
        when(repo.findByStopNameIgnoreCase("GRASAG / Trinity Bus Stop")).thenReturn(List.of(d1));

        var result = service.resolveByNames("Brunei Bus Stop", "GRASAG / Trinity Bus Stop");
        assertTrue(result.containsKey("error"));
        assertEquals("pickup and dropoff are not on the same route", result.get("error"));
    }

    @Test
    void resolvesForwardWithinRoute() {
        // both on route 20
        RouteStop p1 = makeStop(3, 20, "Brunei Bus Stop", 2, "FORWARD");
        RouteStop p2 = makeStop(4, 20, "Brunei Bus Stop", 8, "BACKWARD");
        RouteStop d1 = makeStop(5, 20, "GRASAG / Trinity Bus Stop", 5, "FORWARD");
        RouteStop d2 = makeStop(6, 20, "GRASAG / Trinity Bus Stop", 1, "BACKWARD");

        when(repo.findByStopNameIgnoreCase("Brunei Bus Stop")).thenReturn(List.of(p1, p2));
        when(repo.findByStopNameIgnoreCase("GRASAG / Trinity Bus Stop")).thenReturn(List.of(d1, d2));
        when(repo.findByStopNameIgnoreCaseAndRoute_Id("Brunei Bus Stop", 20)).thenReturn(List.of(p1, p2));
        when(repo.findByStopNameIgnoreCaseAndRoute_Id("GRASAG / Trinity Bus Stop", 20)).thenReturn(List.of(d1, d2));
        when(repo.findByRoute_IdOrderByStopOrderAsc(20)).thenReturn(List.of(p1, d1, p2, d2));

        var result = service.resolveByNames("Brunei Bus Stop", "GRASAG / Trinity Bus Stop");
        assertFalse(result.containsKey("error"));
        assertEquals(20, result.get("route_id"));
        assertEquals("FORWARD", result.get("direction"));
        var pickup = (java.util.Map) result.get("pickupStop");
        var drop = (java.util.Map) result.get("dropoffStop");
        assertEquals(3, pickup.get("stop_id"));
        assertEquals(5, drop.get("stop_id"));
    }

    @Test
    void ambiguousRequiresStopIdWhenMultipleCandidatesSameDirection() {
        RouteStop p1 = makeStop(7, 30, "Brunei Bus Stop", 2, "FORWARD");
        RouteStop p2 = makeStop(8, 30, "Brunei Bus Stop", 4, "FORWARD");
        RouteStop d1 = makeStop(9, 30, "GRASAG / Trinity Bus Stop", 5, "FORWARD");

        when(repo.findByStopNameIgnoreCase("Brunei Bus Stop")).thenReturn(List.of(p1, p2));
        when(repo.findByStopNameIgnoreCase("GRASAG / Trinity Bus Stop")).thenReturn(List.of(d1));
        when(repo.findByStopNameIgnoreCaseAndRoute_Id("Brunei Bus Stop", 30)).thenReturn(List.of(p1, p2));
        when(repo.findByStopNameIgnoreCaseAndRoute_Id("GRASAG / Trinity Bus Stop", 30)).thenReturn(List.of(d1));
        when(repo.findByRoute_IdOrderByStopOrderAsc(30)).thenReturn(List.of(p1, p2, d1));

        var result = service.resolveByNames("Brunei Bus Stop", "GRASAG / Trinity Bus Stop");
        assertTrue(result.containsKey("error"));
        assertTrue(((String)result.get("error")).toLowerCase().contains("provide the stop_id"));
        assertTrue(result.containsKey("matches"));
    }
}

