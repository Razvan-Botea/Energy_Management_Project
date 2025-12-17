package com.example.websocket.config;

import com.example.websocket.config.RabbitMQConfig;
import com.example.websocket.config.dtos.ChatMessageDTO;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class ChatListener {

    private final SimpMessagingTemplate messagingTemplate;

    public ChatListener(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @RabbitListener(queues = RabbitMQConfig.CHAT_QUEUE)
    public void consumeChatMessage(ChatMessageDTO msg) {
        System.out.println("Chat: " + msg.content());

        messagingTemplate.convertAndSend("/topic/public", msg);

        String topic = "/topic/chat/" + msg.senderId();
        messagingTemplate.convertAndSend(topic, msg);
    }
}