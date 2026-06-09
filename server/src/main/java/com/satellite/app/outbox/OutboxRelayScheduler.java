package com.satellite.app.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class OutboxRelayScheduler {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelayScheduler.class);
    private static final int BATCH_SIZE = 100;

    private final OutboxEventRepository outboxEventRepository;
    private final OutboxRelayService outboxRelayService;

    public OutboxRelayScheduler(OutboxEventRepository outboxEventRepository, OutboxRelayService outboxRelayService) {
        this.outboxEventRepository = outboxEventRepository;
        this.outboxRelayService = outboxRelayService;
    }

    @Scheduled(fixedDelayString = "${app.outbox.poll-interval-ms:5000}")
    public void relayPendingEvents() {
        List<OutboxEvent> pending = outboxEventRepository.findByStatusOrderByCreatedAtAsc(
                OutboxStatus.PENDING,
                PageRequest.of(0, BATCH_SIZE)
        );

        for (OutboxEvent outboxEvent : pending) {
            try {
                outboxRelayService.relay(outboxEvent);
            } catch (Exception e) {
                log.error("Outbox: failed to relay event id={}", outboxEvent.getId(), e);
            }
        }
    }
}
