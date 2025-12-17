package com.example.demo.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "monitored_devices")
public class MonitoredDevice {
    @Id
    private UUID id;
    private UUID userId;
    private double maxConsumption;

    public MonitoredDevice() {}

    public MonitoredDevice(UUID id, UUID userId, double maxConsumption) {
        this.id = id;
        this.userId = userId;
        this.maxConsumption = maxConsumption;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public double getMaxConsumption() { return maxConsumption; }
    public void setMaxConsumption(double maxConsumption) { this.maxConsumption = maxConsumption; }
}