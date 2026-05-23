package com.satellite.app.kafka;

import com.satellite.app.model.Satellite;
import com.satellite.events.SatelliteKafkaTopics;
import com.satellite.events.SatelliteLifecycleEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaSatelliteEventPublisher implements SatelliteEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaSatelliteEventPublisher.class);

    private final KafkaTemplate<String, SatelliteLifecycleEvent> kafkaTemplate;

    public KafkaSatelliteEventPublisher(KafkaTemplate<String, SatelliteLifecycleEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publishCreated(Satellite satellite) {
        SatelliteLifecycleEvent event = SatelliteEventMapper.toCreated(satellite);
        kafkaTemplate.send(SatelliteKafkaTopics.CREATED, key(event), event);
        log.info("Kafka: published CREATED satellite id={} name={}", event.satelliteId(), event.satelliteName());
    }

    @Override
    public void publishDeleted(Satellite satellite) {
        SatelliteLifecycleEvent event = SatelliteEventMapper.toDeleted(satellite);
        kafkaTemplate.send(SatelliteKafkaTopics.DELETED, key(event), event);
        log.info("Kafka: published DELETED satellite id={} name={}", event.satelliteId(), event.satelliteName());
    }

    private static String key(SatelliteLifecycleEvent event) {
        return event.satelliteId() != null ? event.satelliteId().toString() : event.satelliteName();
    }
}
