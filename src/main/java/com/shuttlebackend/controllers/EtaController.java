package com.shuttlebackend.controllers;

import com.shuttlebackend.dtos.EtaResponseDto;
import com.shuttlebackend.services.EtaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// OpenAPI
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/api/shuttles")
@RequiredArgsConstructor
@Tag(name = "ETA", description = "Endpoints to calculate ETA for shuttles")
public class EtaController {

    private final EtaService etaService;

    @Operation(summary = "Get ETA for a shuttle from pickup to dropoff",
            description = "Returns ETA and distances.")
    @GetMapping("/{shuttleId}/eta")
    public ResponseEntity<?> getEta(
            @Parameter(description = "Shuttle ID") @PathVariable Integer shuttleId,
            @Parameter(description = "Pickup stop ID") @RequestParam Integer pickupStopId,
            @Parameter(description = "Dropoff stop ID") @RequestParam Integer dropoffStopId
    ) {
        try {
            EtaResponseDto resp = etaService.calculateEta(shuttleId, pickupStopId, dropoffStopId);
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(java.util.Map.of("error", "eta_error", "detail", ex.getMessage()));
        }
    }
}
