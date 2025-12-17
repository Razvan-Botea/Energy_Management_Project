package com.example.loadbalancingservice;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class LoadBalancerService {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.queue.replica.prefix}")
    private String queuePrefix;

    @Value("${app.replica.count}")
    private int replicaCount;

    private final AtomicInteger counter = new AtomicInteger(0);

    public LoadBalancerService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = "${app.queue.central}")
    public void routeMessage(Message message) {
        int replicaId = (Math.abs(counter.getAndIncrement()) % replicaCount) + 1;
        String targetQueue = queuePrefix + replicaId;

        String body = new String(message.getBody(), StandardCharsets.UTF_8);

        System.out.println("LB: Routing to " + targetQueue + " | Content: " + body);

        MessageProperties props = new MessageProperties();
        props.setContentType(MessageProperties.CONTENT_TYPE_JSON);

        Message newMessage = new Message(message.getBody(), props);

        rabbitTemplate.send(targetQueue, newMessage);
    }
}