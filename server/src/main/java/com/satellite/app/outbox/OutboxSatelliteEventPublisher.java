package com.satellite.app.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.satellite.app.kafka.SatelliteEventMapper;
import com.satellite.app.kafka.SatelliteEventPublisher;
import com.satellite.app.model.Satellite;
import com.satellite.events.SatelliteLifecycleEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class OutboxSatelliteEventPublisher implements SatelliteEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxSatelliteEventPublisher.class);

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public OutboxSatelliteEventPublisher(OutboxEventRepository outboxEventRepository, ObjectMapper objectMapper) {
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publishCreated(Satellite satellite) {
        saveOutboxEvent(satellite, SatelliteLifecycleEvent.EVENT_CREATED,
                SatelliteEventMapper.toCreated(satellite, UUID.randomUUID()));
    }

    @Override
    public void publishDeleted(Satellite satellite) {
        saveOutboxEvent(satellite, SatelliteLifecycleEvent.EVENT_DELETED,
                SatelliteEventMapper.toDeleted(satellite, UUID.randomUUID()));
    }

    private void saveOutboxEvent(Satellite satellite, String eventType, SatelliteLifecycleEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            OutboxEvent outboxEvent = new OutboxEvent(
                    UUID.fromString(event.eventId()),
                    satellite.getId(),
                    eventType,
                    payload
            );
            outboxEventRepository.save(outboxEvent);
            log.info("Outbox: queued {} satellite id={} eventId={}", eventType, event.satelliteId(), event.eventId());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Не удалось сериализовать событие спутника", e);
        }
    }
}
