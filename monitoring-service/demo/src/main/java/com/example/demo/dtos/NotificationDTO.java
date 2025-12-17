package com.example.demo.dtos;

import java.util.UUID;

public record NotificationDTO(String message, UUID userId, UUID deviceId) {}