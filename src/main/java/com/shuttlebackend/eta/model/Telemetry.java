package com.shuttlebackend.eta.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "telemetry")
public class Telemetry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String vehicleId;
    private double latitude;
    private double longitude;
    private double speedMetersPerSecond;
    private Instant recordedAt;

    public Telemetry() {}

    public Telemetry(String vehicleId, double latitude, double longitude, double speedMetersPerSecond, Instant recordedAt) {
        this.vehicleId = vehicleId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.speedMetersPerSecond = speedMetersPerSecond;
        this.recordedAt = recordedAt;
    }

    public Long getId() { return id; }
    public String getVehicleId() { return vehicleId; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public double getSpeedMetersPerSecond() { return speedMetersPerSecond; }
    public Instant getRecordedAt() { return recordedAt; }
}

