package com.example.demo.repositories;

import com.example.demo.entities.Measurement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface MeasurementRepository extends JpaRepository<Measurement, UUID> {

    java.util.Optional<Measurement> findByDeviceIdAndTimestamp(UUID deviceId, long timestamp);

    List<Measurement> findByDeviceIdAndTimestampBetween(UUID deviceId, long start, long end);
}