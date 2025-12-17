package com.example.demo.services;

import com.example.demo.RabbitMQConfig;
import com.example.demo.dtos.SensorDataDTO;
import com.example.demo.entities.Measurement;
import com.example.demo.repositories.MeasurementRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import com.example.demo.entities.MonitoredDevice;
import com.example.demo.repositories.MonitoredDeviceRepository;
import com.example.demo.dtos.NotificationDTO;

import java.util.Optional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class MonitoringService {

    private final MeasurementRepository measurementRepository;
    private final MonitoredDeviceRepository monitoredDeviceRepository;
    private final RabbitTemplate rabbitTemplate;

    public MonitoringService(MeasurementRepository measurementRepository,
                             MonitoredDeviceRepository monitoredDeviceRepository,
                             RabbitTemplate rabbitTemplate) {
        this.measurementRepository = measurementRepository;
        this.monitoredDeviceRepository = monitoredDeviceRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = "${app.queue.name}")
    public void consumeSensorData(SensorDataDTO data) {
        System.out.println("Received Message: " + data);

        Instant instant = Instant.ofEpochMilli(data.timestamp());
        long hourTimestamp = instant.truncatedTo(ChronoUnit.HOURS).toEpochMilli();

        Optional<Measurement> existingRecord = measurementRepository
                .findByDeviceIdAndTimestamp(data.deviceId(), hourTimestamp);

        if (existingRecord.isPresent()) {
            Measurement measurement = existingRecord.get();
            measurement.setHourlyConsumption(measurement.getHourlyConsumption() + data.measurementValue());
            measurementRepository.save(measurement);
        } else {
            Measurement newMeasurement = new Measurement(
                    data.deviceId(),
                    hourTimestamp,
                    data.measurementValue()
            );
            measurementRepository.save(newMeasurement);
        }

        MonitoredDevice device = monitoredDeviceRepository.findById(data.deviceId()).orElse(null);

        if (device != null) {
            Optional<Measurement> updatedRecord = measurementRepository
                    .findByDeviceIdAndTimestamp(data.deviceId(), hourTimestamp);

            if (updatedRecord.isPresent()) {
                double totalConsumption = updatedRecord.get().getHourlyConsumption();

                if (totalConsumption > device.getMaxConsumption()) {
                    System.out.println("OVERCONSUMPTION DETECTED!");

                    String msg = "Device " + device.getId() + " exceeded limit! Current: " + totalConsumption + " / Max: " + device.getMaxConsumption();
                    NotificationDTO notification = new NotificationDTO(msg, device.getUserId(), device.getId());

                    rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "notification.overconsumption", notification);
                }
            }
        }
    }
}