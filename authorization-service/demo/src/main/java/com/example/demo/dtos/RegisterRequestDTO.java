package com.example.demo.dtos;

import jakarta.validation.constraints.NotBlank;

public record RegisterRequestDTO(
        @NotBlank String username,
        @NotBlank String password,
        @NotBlank String role
) {}
