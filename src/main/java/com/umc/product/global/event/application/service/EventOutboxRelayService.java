package com.umc.product.global.event.application.service;

import com.umc.product.global.event.adapter.out.EventPayloadDeserializer;
import com.umc.product.global.event.application.port.out.LoadEventOutboxPort;
import com.umc.product.global.event.domain.DomainEvent;
import com.umc.product.global.event.domain.EventOutbox;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventOutboxRelayService {

    private static final Duration BASE_BACKOFF = Duration.ofSeconds(5);
    private static final Duration MAX_BACKOFF = Duration.ofMinutes(5);

    private final LoadEventOutboxPort loadEventOutboxPort;
    private final EventPayloadDeserializer deserializer;
    private final ApplicationEventPublisher eventPublisher;
    private final int batchSize;
    private final int maxAttempts;

    public EventOutboxRelayService(
        LoadEventOutboxPort loadEventOutboxPort,
        EventPayloadDeserializer deserializer,
        ApplicationEventPublisher eventPublisher,
        @Value("${app.event-outbox.batch-size:100}") int batchSize,
        @Value("${app.event-outbox.max-attempts:5}") int maxAttempts
    ) {
        this.loadEventOutboxPort = loadEventOutboxPort;
        this.deserializer = deserializer;
        this.eventPublisher = eventPublisher;
        this.batchSize = batchSize;
        this.maxAttempts = maxAttempts;
    }

    @Transactional
    public void relay() {
        Instant now = Instant.now();
        List<EventOutbox> outboxes = loadEventOutboxPort.listPublishable(batchSize, now);
        for (EventOutbox outbox : outboxes) {
            relayOne(outbox);
        }
    }

    private void relayOne(EventOutbox outbox) {
        try {
            DomainEvent event = deserializer.deserialize(outbox);
            eventPublisher.publishEvent(event);
            outbox.markPublished();
        } catch (RuntimeException e) {
            outbox.recordFailure(errorMessage(e), nextAttemptAt(outbox), maxAttempts);
        }
    }

    private Instant nextAttemptAt(EventOutbox outbox) {
        long multiplier = 1L << Math.min(outbox.getAttempts(), 6);
        Duration backoff = BASE_BACKOFF.multipliedBy(multiplier);
        if (backoff.compareTo(MAX_BACKOFF) > 0) {
            backoff = MAX_BACKOFF;
        }
        return Instant.now().plus(backoff);
    }

    private String errorMessage(RuntimeException e) {
        if (e.getMessage() == null || e.getMessage().isBlank()) {
            return e.getClass().getName();
        }
        return e.getMessage();
    }
}
