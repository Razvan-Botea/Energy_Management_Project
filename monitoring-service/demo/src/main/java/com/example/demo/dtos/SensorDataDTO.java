package com.example.demo.dtos;

import java.util.UUID;

public record SensorDataDTO(long timestamp, UUID deviceId, double measurementValue) {}