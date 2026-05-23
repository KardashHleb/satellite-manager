package com.satellite.app.kafka;

import com.satellite.app.model.Satellite;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "false")
public class NoOpSatelliteEventPublisher implements SatelliteEventPublisher {

    @Override
    public void publishCreated(Satellite satellite) {
        // Kafka отключён (например, в unit-тестах)
    }

    @Override
    public void publishDeleted(Satellite satellite) {
        // Kafka отключён
    }
}
