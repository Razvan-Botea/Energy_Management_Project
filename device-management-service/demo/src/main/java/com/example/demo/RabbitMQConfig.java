package com.example.demo;

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
    public static final String USER_SYNC_QUEUE = "user_sync_queue_device";

    public static final String SENSOR_QUEUE = "sensor_queue";
    public static final String DEVICE_SYNC_QUEUE = "device_sync_queue";

    public static final String ROUTING_KEY_SENSOR = "device.measurement";
    public static final String ROUTING_KEY_DEVICE_SYNC = "device.*"; // Prinde create, update, delete

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue sensorQueue() {
        return new Queue(SENSOR_QUEUE);
    }

    @Bean
    public Queue deviceSyncQueue() {
        return new Queue(DEVICE_SYNC_QUEUE);
    }

    @Bean
    public Queue userSyncQueue() {
        return new Queue(USER_SYNC_QUEUE);
    }

    @Bean
    public Binding bindingUserSync(Queue userSyncQueue, TopicExchange exchange) {
        return BindingBuilder.bind(userSyncQueue).to(exchange).with("user.*");
    }

    @Bean
    public Binding bindingSensor(Queue sensorQueue, TopicExchange exchange) {
        return BindingBuilder.bind(sensorQueue).to(exchange).with(ROUTING_KEY_SENSOR);
    }

    @Bean
    public Binding bindingDeviceSync(Queue deviceSyncQueue, TopicExchange exchange) {
        return BindingBuilder.bind(deviceSyncQueue).to(exchange).with(ROUTING_KEY_DEVICE_SYNC);
    }

    @Bean
    public MessageConverter converter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter());
        return template;
    }
}