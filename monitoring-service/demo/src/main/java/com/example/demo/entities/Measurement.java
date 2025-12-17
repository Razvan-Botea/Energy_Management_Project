package com.example.demo.entities;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "measurements")
public class Measurement implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "device_id", nullable = false)
    private UUID deviceId;

    @Column(name = "timestamp", nullable = false)
    private long timestamp;

    @Column(name = "hourly_consumption", nullable = false)
    private double hourlyConsumption;

    public Measurement() {}

    public Measurement(UUID deviceId, long timestamp, double hourlyConsumption) {
        this.deviceId = deviceId;
        this.timestamp = timestamp;
        this.hourlyConsumption = hourlyConsumption;
    }

    // Getters and Setters ...
    public UUID getId() { return id; }
    public UUID getDeviceId() { return deviceId; }
    public void setDeviceId(UUID deviceId) { this.deviceId = deviceId; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public double getHourlyConsumption() { return hourlyConsumption; }
    public void setHourlyConsumption(double hourlyConsumption) { this.hourlyConsumption = hourlyConsumption; }
}