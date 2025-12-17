package com.example.demo;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(title = "Device Management Service", version = "v1", description = "Serviciul de management al dispozitivelor"))
@SecurityScheme(
        name = "Bearer Authentication", // Un nume la alegere
        type = SecuritySchemeType.HTTP,  // Tipul schemei
        bearerFormat = "JWT",            // Formatul
        scheme = "bearer"                // Schema HTTP
)
public class OpenApiConfig {
    // Această clasă este goală intenționat.
    // Doar adnotările de mai sus sunt necesare.
}