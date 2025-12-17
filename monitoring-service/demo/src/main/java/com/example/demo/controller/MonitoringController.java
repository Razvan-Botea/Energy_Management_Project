package com.example.demo.controller;

import com.example.demo.entities.Measurement;
import com.example.demo.repositories.MeasurementRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/monitoring")
public class MonitoringController {

    private final MeasurementRepository measurementRepository;

    public MonitoringController(MeasurementRepository measurementRepository) {
        this.measurementRepository = measurementRepository;
    }

    /**
     * @param deviceId ID-ul dispozitivului
     * @param timestamp Timpul în milisecunde reprezentând ora 00:00 a zilei selectate
     */
    @GetMapping("/{deviceId}/{timestamp}")
    public ResponseEntity<List<Measurement>> getDailyConsumption(
            @PathVariable UUID deviceId,
            @PathVariable long timestamp) {

        long startOfDay = timestamp;
        long endOfDay = timestamp + 86400000;

        System.out.println("=================================");
        System.out.println("REQUEST RECEIVED:");
        System.out.println("Device ID from URL: " + deviceId);
        System.out.println("Search Start Timestamp: " + startOfDay);
        System.out.println("Search End Timestamp:   " + endOfDay);

        long myDbTimestamp = 1764018000000L;
        boolean isInside = (myDbTimestamp >= startOfDay && myDbTimestamp <= endOfDay);
        System.out.println("Is my DB data (" + myDbTimestamp + ") inside this range? -> " + isInside);

        List<Measurement> measurements = measurementRepository.findByDeviceIdAndTimestampBetween(
                deviceId,
                startOfDay,
                endOfDay
        );
        System.out.println("Records found: " + measurements.size());
        System.out.println("=================================");

        return ResponseEntity.ok(measurements);
    }
}