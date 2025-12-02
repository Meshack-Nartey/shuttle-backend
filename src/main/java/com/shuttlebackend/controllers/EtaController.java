package com.shuttlebackend.controllers;

import com.shuttlebackend.dtos.EtaResponseDto;
import com.shuttlebackend.services.EtaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shuttles")
@RequiredArgsConstructor
public class EtaController {

    private final EtaService etaService;

    @GetMapping("/{shuttleId}/eta")
    public ResponseEntity<?> getEta(
            @PathVariable Integer shuttleId,
            @RequestParam Integer pickupStopId,
            @RequestParam Integer dropoffStopId
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
