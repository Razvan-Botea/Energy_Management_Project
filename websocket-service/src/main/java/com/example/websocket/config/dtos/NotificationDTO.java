package com.example.websocket.config.dtos;

import java.util.UUID;

public record NotificationDTO(
        String message,
        UUID userId,
        UUID deviceId
) {}