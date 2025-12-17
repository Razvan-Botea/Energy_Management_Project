package com.example.demo.dtos;

public record LoginResponseDTO(
        String username,
        String token,
        String role
) {}
