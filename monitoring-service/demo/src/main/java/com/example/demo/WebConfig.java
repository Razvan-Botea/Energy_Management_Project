package com.example.demo; // <-- Make sure this package is correct!
// For device-service, it would be: package com.example.devicemanagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // Apply to all paths in this service (e.g., /users/**, /devices/**)
                registry.addMapping("/**")
                        // Allow your React app
                        .allowedOrigins("http://localhost:3000")
                        // Allow all necessary methods
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        // Allow the headers your React app sends
                        .allowedHeaders("Authorization", "Content-Type");
            }
        };
    }
}