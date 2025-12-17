package com.example.demo.services;

import com.example.demo.RabbitMQConfig;
import com.example.demo.dtos.UserDTO;
import com.example.demo.dtos.UserDetailsDTO;
import com.example.demo.dtos.UserSyncDTO;
import com.example.demo.dtos.builders.UserBuilder;
import com.example.demo.entities.User;
import com.example.demo.handlers.exceptions.model.ResourceNotFoundException;
import com.example.demo.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    private final RabbitTemplate rabbitTemplate;

    private final String authServiceUrl = "http://auth-service:8080/api/auth/register";
    private final String authServiceDeleteUrl = "http://auth-service:8080/api/auth/delete/";

    @Autowired
    public UserService(UserRepository userRepository, RestTemplate restTemplate, RabbitTemplate rabbitTemplate) {
        this.userRepository = userRepository;
        this.restTemplate = restTemplate;
        this.rabbitTemplate = rabbitTemplate;
    }

    public List<UserDTO> findUsers() {
        List<User> userList = userRepository.findAll();
        return userList.stream()
                .map(UserBuilder::toUserDTO)
                .collect(Collectors.toList());
    }

    public UserDetailsDTO findUserById(UUID id) {
        Optional<User> userOptional = userRepository.findById(id);
        if (!userOptional.isPresent()) {
            LOGGER.error("User with id {} was not found in db", id);
            throw new ResourceNotFoundException(User.class.getSimpleName() + " with id: " + id);
        }
        return UserBuilder.toUserDetailsDTO(userOptional.get());
    }

    public UUID insert(UserDetailsDTO userDTO) {
        userRepository.findByUsername(userDTO.getUsername())
                .ifPresent(user -> {
                    LOGGER.error("User with username {} already exists", user.getUsername());
                    throw new IllegalArgumentException("Username already exists: " + user.getUsername());
                });

        User user = UserBuilder.toEntity(userDTO);
        user = userRepository.save(user);
        LOGGER.debug("User with id {} was inserted in db", user.getId());

        try {
            RegisterRequestDTO registerRequest = new RegisterRequestDTO(
                    userDTO.getUsername(),
                    userDTO.getPassword(),
                    userDTO.getRole()
            );
            restTemplate.postForEntity(authServiceUrl, registerRequest, String.class);
            LOGGER.debug("Credential for user {} registered with auth-service", user.getUsername());

            UserSyncDTO syncDTO = new UserSyncDTO(user.getId(), user.getUsername(), user.getRole());
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "user.create", syncDTO);
            LOGGER.debug("Sent sync message for created user: {}", user.getUsername());

        } catch (Exception e) {
            LOGGER.error("Failed to register credential for user {}: {}", user.getUsername(), e.getMessage());
            userRepository.delete(user);
            throw new RuntimeException("Failed to create user: Could not register credentials.");
        }
        return user.getId();
    }

    public UserDetailsDTO update(UUID id, UserDetailsDTO userDetailsDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    LOGGER.error("User with id {} was not found in db", id);
                    return new ResourceNotFoundException(User.class.getSimpleName() + " with id: " + id);
                });

        user.setName(userDetailsDTO.getName());
        user.setRole(userDetailsDTO.getRole());
        user.setUsername(userDetailsDTO.getUsername());

        if (userDetailsDTO.getPassword() != null && !userDetailsDTO.getPassword().isEmpty()) {
            user.setPassword(userDetailsDTO.getPassword());
        }

        user = userRepository.save(user);
        LOGGER.debug("User with id {} was updated in db", user.getId());

        return UserBuilder.toUserDetailsDTO(user);
    }

    public void delete(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    LOGGER.error("User with id {} was not found in db", id);
                    return new ResourceNotFoundException(User.class.getSimpleName() + " with id: " + id);
                });

        String username = user.getUsername();
        userRepository.delete(user);
        LOGGER.debug("User with id {} was deleted from db", user.getId());

        try {
            restTemplate.delete(authServiceDeleteUrl + username);
            LOGGER.debug("Credential for user {} deleted from auth-service", username);
        } catch (Exception e) {
            LOGGER.warn("Failed to delete credential for user {}: {}", username, e.getMessage());
        }

        UserSyncDTO syncDTO = new UserSyncDTO(id, null, null);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "user.delete", syncDTO);
        LOGGER.debug("Sent sync message for deleted user ID: {}", id);
    }

    private record RegisterRequestDTO(
            String username,
            String password,
            String role
    ) {}
}