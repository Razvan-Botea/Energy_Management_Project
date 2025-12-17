package com.example.demo.services;

import com.example.demo.RabbitMQConfig; // Asigură-te că pachetul e corect
import com.example.demo.dtos.DeviceSyncDTO;
import com.example.demo.entities.MonitoredDevice;
import com.example.demo.repositories.MonitoredDeviceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class DeviceEventsListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceEventsListener.class);
    private final MonitoredDeviceRepository repository;

    public DeviceEventsListener(MonitoredDeviceRepository repository) {
        this.repository = repository;
    }

    @RabbitListener(queues = "#{deviceSyncQueue.name}")
    public void consumeDeviceSync(DeviceSyncDTO dto) {
        LOGGER.info("Syncing device: {} | Max: {}", dto.id(), dto.maxConsumption());

        if (dto.id() == null) {
            LOGGER.error("Received sync message with NULL ID");
            return;
        }

        MonitoredDevice device = new MonitoredDevice();
        device.setId(dto.id());
        device.setUserId(dto.userId());
        device.setMaxConsumption(dto.maxConsumption());

        repository.save(device);
    }
}