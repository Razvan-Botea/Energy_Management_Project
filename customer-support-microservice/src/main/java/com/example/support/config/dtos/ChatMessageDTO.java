package com.example.support.config.dtos;

import java.util.UUID;

public record ChatMessageDTO(
        UUID senderId,
        String content,
        boolean isFromAdmin, // true if Admin sent it, false if User sent it
        long timestamp
) {}