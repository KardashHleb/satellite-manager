package com.satellite.app.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.satellite.events.SatelliteKafkaTopics;
import com.satellite.events.SatelliteLifecycleEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class OutboxRelayService {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelayService.class);

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, SatelliteLifecycleEvent> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public OutboxRelayService(
            OutboxEventRepository outboxEventRepository,
            KafkaTemplate<String, SatelliteLifecycleEvent> kafkaTemplate,
            ObjectMapper objectMapper
    ) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void relay(OutboxEvent outboxEvent) throws Exception {
        SatelliteLifecycleEvent event = objectMapper.readValue(outboxEvent.getPayload(), SatelliteLifecycleEvent.class);
        String topic = resolveTopic(outboxEvent.getEventType());
        kafkaTemplate.send(topic, messageKey(event), event).get();
        outboxEvent.setStatus(OutboxStatus.SENT);
        outboxEventRepository.save(outboxEvent);
        log.info("Outbox: sent {} eventId={} satelliteId={}", outboxEvent.getEventType(), event.eventId(), event.satelliteId());
    }

    private static String resolveTopic(String eventType) {
        if (SatelliteLifecycleEvent.EVENT_CREATED.equals(eventType)) {
            return SatelliteKafkaTopics.CREATED;
        }
        if (SatelliteLifecycleEvent.EVENT_DELETED.equals(eventType)) {
            return SatelliteKafkaTopics.DELETED;
        }
        throw new IllegalArgumentException("Неизвестный тип события outbox: " + eventType);
    }

    private static String messageKey(SatelliteLifecycleEvent event) {
        return event.satelliteId() != null ? event.satelliteId().toString() : event.satelliteName();
    }
}
