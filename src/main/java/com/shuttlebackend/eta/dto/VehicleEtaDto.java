package com.shuttlebackend.eta.dto;

import java.time.Instant;
import java.util.Objects;

public class VehicleEtaDto {
    private String vehicleId;
    private Instant etaTimestamp;
    private long etaMillis;
    private double distanceMeters;
    private String reason;

    public VehicleEtaDto() {}

    public VehicleEtaDto(String vehicleId, Instant etaTimestamp, long etaMillis, double distanceMeters, String reason) {
        this.vehicleId = vehicleId;
        this.etaTimestamp = etaTimestamp;
        this.etaMillis = etaMillis;
        this.distanceMeters = distanceMeters;
        this.reason = reason;
    }

    public String getVehicleId() { return vehicleId; }
    public Instant getEtaTimestamp() { return etaTimestamp; }
    public long getEtaMillis() { return etaMillis; }
    public double getDistanceMeters() { return distanceMeters; }
    public String getReason() { return reason; }

    public void setVehicleId(String vehicleId) { this.vehicleId = vehicleId; }
    public void setEtaTimestamp(Instant etaTimestamp) { this.etaTimestamp = etaTimestamp; }
    public void setEtaMillis(long etaMillis) { this.etaMillis = etaMillis; }
    public void setDistanceMeters(double distanceMeters) { this.distanceMeters = distanceMeters; }
    public void setReason(String reason) { this.reason = reason; }

    @Override
    public boolean equals(Object o) { if (this == o) return true; if (o == null || getClass() != o.getClass()) return false; VehicleEtaDto that = (VehicleEtaDto) o; return etaMillis == that.etaMillis && Double.compare(that.distanceMeters, distanceMeters) == 0 && Objects.equals(vehicleId, that.vehicleId) && Objects.equals(etaTimestamp, that.etaTimestamp) && Objects.equals(reason, that.reason); }
    @Override
    public int hashCode() { return Objects.hash(vehicleId, etaTimestamp, etaMillis, distanceMeters, reason); }
}

