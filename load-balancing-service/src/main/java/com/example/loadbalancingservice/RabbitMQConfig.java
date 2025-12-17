package com.example.loadbalancingservice; // <--- This fixes "Missing package statement"

import org.springframework.amqp.core.Queue; // <--- Fixes "Cannot resolve symbol 'Queue'"
import org.springframework.beans.factory.annotation.Value; // <--- Fixes "Cannot resolve symbol 'Value'"
import org.springframework.context.annotation.Bean; // <--- Fixes "Cannot resolve symbol 'Bean'"
import org.springframework.context.annotation.Configuration; // <--- Fixes "Cannot resolve symbol 'Configuration'"

@Configuration
public class RabbitMQConfig {

    @Value("${app.queue.central}")
    private String centralQueueName;

    @Bean
    public Queue centralQueue() {
        return new Queue(centralQueueName, true);
    }
}