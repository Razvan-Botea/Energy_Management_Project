package com.example.demo.services;

import com.example.demo.RabbitMQConfig;
import com.example.demo.dtos.DeviceDTO;
import com.example.demo.dtos.DeviceDetailsDTO;
import com.example.demo.dtos.DeviceSyncDTO;
import com.example.demo.dtos.builders.DeviceBuilder;
import com.example.demo.entities.Device;
import com.example.demo.handlers.exceptions.model.ResourceNotFoundException;
import com.example.demo.repositories.DeviceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DeviceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceService.class);
    private final DeviceRepository deviceRepository;
    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public DeviceService(DeviceRepository deviceRepository, RabbitTemplate rabbitTemplate) {
        this.deviceRepository = deviceRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public List<DeviceDTO> findDevices() {
        List<Device> deviceList = deviceRepository.findAll();
        return deviceList.stream()
                .map(DeviceBuilder::toDeviceDTO)
                .collect(Collectors.toList());
    }

    public DeviceDetailsDTO findDeviceById(UUID id) {
        Optional<Device> deviceOptional = deviceRepository.findById(id);
        if (!deviceOptional.isPresent()) {
            LOGGER.error("Device with id {} was not found in db", id);
            throw new ResourceNotFoundException(Device.class.getSimpleName() + " with id: " + id);
        }
        return DeviceBuilder.toDeviceDetailsDTO(deviceOptional.get());
    }

    public UUID insert(DeviceDetailsDTO deviceDTO) {
        Device device = DeviceBuilder.toEntity(deviceDTO);
        device = deviceRepository.save(device);

        DeviceSyncDTO syncDTO = new DeviceSyncDTO(device.getId(), device.getUserId(), device.getMaximumConsumption());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "device.create", syncDTO);
        LOGGER.debug("Sent sync message for created device: {}", device.getId());

        LOGGER.debug("Device with id {} was inserted in db", device.getId());
        return device.getId();
    }

    public DeviceDetailsDTO update(UUID id, DeviceDetailsDTO deviceDetailsDTO) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> {
                    LOGGER.error("Device with id {} was not found in db", id);
                    return new ResourceNotFoundException(Device.class.getSimpleName() + " with id: " + id);
                });

        device.setName(deviceDetailsDTO.getName());
        device.setAddress(deviceDetailsDTO.getAddress());
        device.setMaximumConsumption(deviceDetailsDTO.getMaximumConsumption());
        device.setUserId(deviceDetailsDTO.getUserId());

        device = deviceRepository.save(device);

        DeviceSyncDTO syncDTO = new DeviceSyncDTO(device.getId(), device.getUserId(), device.getMaximumConsumption());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "device.update", syncDTO);
        LOGGER.debug("Sent sync message for updated device: {}", device.getId());

        LOGGER.debug("Device with id {} was updated in db", device.getId());
        return DeviceBuilder.toDeviceDetailsDTO(device);
    }

    public void delete(UUID id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> {
                    LOGGER.error("Device with id {} was not found in db", id);
                    return new ResourceNotFoundException(Device.class.getSimpleName() + " with id: " + id);
                });
        deviceRepository.delete(device);

        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "device.delete", id);
        LOGGER.debug("Sent sync message for deleted device: {}", id);

        LOGGER.debug("Device with id {} was deleted from db", id);
    }

    public List<DeviceDTO> findDevicesByUserId(UUID userId) {
        List<Device> deviceList = deviceRepository.findByUserId(userId);
        return deviceList.stream()
                .map(DeviceBuilder::toDeviceDTO)
                .collect(Collectors.toList());
    }
}