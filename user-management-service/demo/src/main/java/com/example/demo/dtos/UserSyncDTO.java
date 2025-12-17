package com.example.demo.dtos;
import java.util.UUID;

public record UserSyncDTO(UUID id, String username, String role) {}