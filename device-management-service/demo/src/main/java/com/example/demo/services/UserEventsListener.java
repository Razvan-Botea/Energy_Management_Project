package com.example.demo.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.demo.RabbitMQConfig;
import com.example.demo.dtos.UserSyncDTO;
import com.example.demo.entities.LocalUser;
import com.example.demo.repositories.DeviceRepository;
import com.example.demo.repositories.LocalUserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class UserEventsListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserEventsListener.class);

    private final LocalUserRepository localUserRepository;
    private final DeviceRepository deviceRepository;
    private final ObjectMapper objectMapper;

    public UserEventsListener(LocalUserRepository localUserRepository, DeviceRepository deviceRepository) {
        this.localUserRepository = localUserRepository;
        this.deviceRepository = deviceRepository;
        this.objectMapper = new ObjectMapper();
    }

    @RabbitListener(queues = RabbitMQConfig.USER_SYNC_QUEUE)
    @Transactional
    public void consumeUserEvents(Message message) {
        try {
            String jsonBody = new String(message.getBody(), StandardCharsets.UTF_8);
            LOGGER.info("Received Raw Message: {}", jsonBody);

            UserSyncDTO dto = objectMapper.readValue(jsonBody, UserSyncDTO.class);

            String routingKey = message.getMessageProperties().getReceivedRoutingKey();

            if (routingKey.contains("delete")) {
                LOGGER.info("Syncing DELETE for user ID: {}", dto.id());
                deviceRepository.deleteByUserId(dto.id());
                localUserRepository.deleteById(dto.id());
            } else {
                LOGGER.info("Syncing CREATE/UPDATE for user: {}", dto.username());
                localUserRepository.save(new LocalUser(dto.id()));
            }

        } catch (Exception e) {
            LOGGER.error("Failed to process user sync message: {}", e.getMessage());
            e.printStackTrace();
        }
    }
}