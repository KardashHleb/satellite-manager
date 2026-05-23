package com.satellite.telemetry.kafka;

import com.satellite.events.SatelliteKafkaTopics;
import com.satellite.events.SatelliteLifecycleEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class SatelliteLifecycleKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(SatelliteLifecycleKafkaListener.class);

    private final SatelliteRegistry satelliteRegistry;

    public SatelliteLifecycleKafkaListener(SatelliteRegistry satelliteRegistry) {
        this.satelliteRegistry = satelliteRegistry;
    }

    @KafkaListener(
            topics = SatelliteKafkaTopics.CREATED,
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void onSatelliteCreated(SatelliteLifecycleEvent event) {
        satelliteRegistry.registerCreated(event);
        log.info("Kafka: satellite CREATED name={} constellation={}",
                event.satelliteName(), event.constellationName());
    }

    @KafkaListener(
            topics = SatelliteKafkaTopics.DELETED,
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void onSatelliteDeleted(SatelliteLifecycleEvent event) {
        satelliteRegistry.registerDeleted(event);
        log.info("Kafka: satellite DELETED name={} constellation={}",
                event.satelliteName(), event.constellationName());
    }
}
