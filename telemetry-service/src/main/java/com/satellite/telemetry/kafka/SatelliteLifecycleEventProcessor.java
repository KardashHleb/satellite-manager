package com.satellite.telemetry.kafka;

import com.satellite.events.SatelliteLifecycleEvent;
import com.satellite.telemetry.inbox.InboxEvent;
import com.satellite.telemetry.inbox.InboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class SatelliteLifecycleEventProcessor {

    private static final Logger log = LoggerFactory.getLogger(SatelliteLifecycleEventProcessor.class);

    private final InboxEventRepository inboxEventRepository;
    private final SatelliteRegistry satelliteRegistry;

    public SatelliteLifecycleEventProcessor(
            InboxEventRepository inboxEventRepository,
            SatelliteRegistry satelliteRegistry
    ) {
        this.inboxEventRepository = inboxEventRepository;
        this.satelliteRegistry = satelliteRegistry;
    }

    @Transactional
    public void process(SatelliteLifecycleEvent event) {
        if (event.eventId() == null || event.eventId().isBlank()) {
            log.warn("Kafka: skip event without eventId type={} satelliteId={}", event.eventType(), event.satelliteId());
            return;
        }

        UUID eventId = UUID.fromString(event.eventId());
        if (inboxEventRepository.existsByEventId(eventId)) {
            log.debug("Kafka: skip duplicate eventId={}", eventId);
            return;
        }

        try {
            inboxEventRepository.saveAndFlush(new InboxEvent(
                    eventId,
                    event.satelliteId(),
                    event.eventType()
            ));
        } catch (DataIntegrityViolationException ex) {
            log.debug("Kafka: concurrent duplicate eventId={}", eventId);
            return;
        }

        if (SatelliteLifecycleEvent.EVENT_CREATED.equals(event.eventType())) {
            satelliteRegistry.registerCreated(event);
        } else if (SatelliteLifecycleEvent.EVENT_DELETED.equals(event.eventType())) {
            satelliteRegistry.registerDeleted(event);
        } else {
            throw new IllegalArgumentException("Неизвестный тип события: " + event.eventType());
        }
    }
}
