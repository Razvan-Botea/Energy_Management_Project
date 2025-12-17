package com.example.websocket.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String EXCHANGE_NAME = "energy-exchange";
    public static final String NOTIFICATION_QUEUE = "notification_queue";

    @Bean
    public TopicExchange exchange() { return new TopicExchange(EXCHANGE_NAME); }

    @Bean
    public Queue notificationQueue() { return new Queue(NOTIFICATION_QUEUE); }

    @Bean
    public Binding bindingNotification(Queue notificationQueue, TopicExchange exchange) {
        return BindingBuilder.bind(notificationQueue).to(exchange).with("notification.#");
    }

    @Bean
    public MessageConverter converter() { return new Jackson2JsonMessageConverter(); }

    public static final String CHAT_QUEUE = "chat_queue";

    @Bean
    public Queue chatQueue() {
        return new Queue(CHAT_QUEUE);
    }

    @Bean
    public Binding bindingChat(Queue chatQueue, TopicExchange exchange) {
        return BindingBuilder.bind(chatQueue).to(exchange).with("chat.#");
    }
}