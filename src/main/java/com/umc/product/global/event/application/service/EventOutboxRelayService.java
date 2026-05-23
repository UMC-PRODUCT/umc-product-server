package com.umc.product.global.event.application.service;

import com.umc.product.global.event.adapter.out.EventPayloadDeserializer;
import com.umc.product.global.event.application.port.out.LoadEventOutboxPort;
import com.umc.product.global.event.application.port.out.SaveEventOutboxPort;
import com.umc.product.global.event.domain.DomainEvent;
import com.umc.product.global.event.domain.EventOutbox;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class EventOutboxRelayService {

    private static final Duration BASE_BACKOFF = Duration.ofSeconds(5);
    private static final Duration MAX_BACKOFF = Duration.ofMinutes(5);
    private static final Duration PROCESSING_LEASE = Duration.ofMinutes(5);

    private final LoadEventOutboxPort loadEventOutboxPort;
    private final SaveEventOutboxPort saveEventOutboxPort;
    private final EventPayloadDeserializer deserializer;
    private final ApplicationEventPublisher eventPublisher;
    private final TransactionTemplate transactionTemplate;
    private final int batchSize;
    private final int maxAttempts;

    public EventOutboxRelayService(
        LoadEventOutboxPort loadEventOutboxPort,
        SaveEventOutboxPort saveEventOutboxPort,
        EventPayloadDeserializer deserializer,
        ApplicationEventPublisher eventPublisher,
        PlatformTransactionManager transactionManager,
        @Value("${app.event-outbox.batch-size:100}") int batchSize,
        @Value("${app.event-outbox.max-attempts:5}") int maxAttempts
    ) {
        this.loadEventOutboxPort = loadEventOutboxPort;
        this.saveEventOutboxPort = saveEventOutboxPort;
        this.deserializer = deserializer;
        this.eventPublisher = eventPublisher;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.batchSize = batchSize;
        this.maxAttempts = maxAttempts;
    }

    public void relay() {
        List<EventOutbox> outboxes = claimPublishable();
        for (EventOutbox outbox : outboxes) {
            relayOne(outbox);
        }
    }

    private List<EventOutbox> claimPublishable() {
        List<EventOutbox> outboxes = transactionTemplate.execute(status -> {
            Instant now = Instant.now();
            List<EventOutbox> publishableOutboxes = loadEventOutboxPort.listPublishable(batchSize, now);
            Instant leaseUntil = now.plus(PROCESSING_LEASE);
            publishableOutboxes.forEach(outbox -> outbox.markProcessing(leaseUntil));
            saveEventOutboxPort.saveAll(publishableOutboxes);
            return publishableOutboxes;
        });
        return outboxes == null ? List.of() : outboxes;
    }

    private void relayOne(EventOutbox outbox) {
        try {
            publish(outbox);
            markPublished(outbox);
        } catch (RuntimeException e) {
            recordFailure(outbox, e);
        }
    }

    private void publish(EventOutbox outbox) {
        transactionTemplate.executeWithoutResult(status -> {
            DomainEvent event = deserializer.deserialize(outbox);
            eventPublisher.publishEvent(event);
        });
    }

    private void markPublished(EventOutbox outbox) {
        transactionTemplate.executeWithoutResult(status -> {
            outbox.markPublished();
            saveEventOutboxPort.save(outbox);
        });
    }

    private void recordFailure(EventOutbox outbox, RuntimeException e) {
        transactionTemplate.executeWithoutResult(status -> {
            outbox.recordFailure(errorMessage(e), nextAttemptAt(outbox), maxAttempts);
            saveEventOutboxPort.save(outbox);
        });
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
