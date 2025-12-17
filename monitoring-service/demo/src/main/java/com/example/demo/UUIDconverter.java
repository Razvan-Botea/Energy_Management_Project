// Asigură-te că acest pachet este corect pentru proiectul tău
package com.example.demo;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.UUID;

@Configuration
public class UUIDconverter implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        // Această funcție îi spune lui Spring (și springdoc) cum să convertească
        // un String dintr-o cale URL (ex: "a1b2c3d4-...") într-un obiect UUID.
        registry.addConverter(new Converter<String, UUID>() {
            @Override
            public UUID convert(String source) {
                if (source == null || source.isEmpty()) {
                    return null;
                }
                try {
                    return UUID.fromString(source);
                } catch (IllegalArgumentException e) {
                    // Gestionează eroarea dacă string-ul nu este un UUID valid
                    return null;
                }
            }
        });
    }
}