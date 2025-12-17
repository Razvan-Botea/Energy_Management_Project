package com.example.demo.dtos;

import java.util.UUID;

public record DeviceSyncDTO(UUID id, UUID userId, float maxConsumption) {}