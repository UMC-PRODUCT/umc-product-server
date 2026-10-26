package com.umc.product.global.event.application.service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import com.umc.product.global.event.adapter.out.EventPayloadDeserializer;
import com.umc.product.global.event.application.port.out.LoadEventOutboxPort;
import com.umc.product.global.event.application.port.out.OutboxEventSelection;
import com.umc.product.global.event.application.port.out.SaveEventOutboxPort;
import com.umc.product.global.event.domain.DomainEvent;
import com.umc.product.global.event.domain.EventOutbox;
import com.umc.product.global.event.domain.OutboxDispatchMode;
import com.umc.product.global.observability.ObservabilityErrorSanitizer;
import com.umc.product.global.observability.W3CTraceparent;

import io.micrometer.tracing.Link;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EventOutboxRelayService {

    private static final Duration BASE_BACKOFF = Duration.ofSeconds(5);
    private static final Duration MAX_BACKOFF = Duration.ofMinutes(5);
    private static final Duration PROCESSING_LEASE = Duration.ofMinutes(5);
    private static final String RELAY_SPAN_NAME = "outbox.relay.publish";

    private final LoadEventOutboxPort loadEventOutboxPort;
    private final SaveEventOutboxPort saveEventOutboxPort;
    private final EventPayloadDeserializer deserializer;
    private final ApplicationEventPublisher eventPublisher;
    private final Tracer tracer;
    private final EventOutboxRelayMetrics relayMetrics;
    private final TransactionTemplate transactionTemplate;
    private final int maxAttempts;

    @Autowired
    public EventOutboxRelayService(
        LoadEventOutboxPort loadEventOutboxPort,
        SaveEventOutboxPort saveEventOutboxPort,
        EventPayloadDeserializer deserializer,
        ApplicationEventPublisher eventPublisher,
        PlatformTransactionManager transactionManager,
        ObjectProvider<Tracer> tracerProvider,
        EventOutboxRelayMetrics relayMetrics,
        @Value("${app.event-outbox.max-attempts:5}") int maxAttempts
    ) {
        this(
            loadEventOutboxPort,
            saveEventOutboxPort,
            deserializer,
            eventPublisher,
            transactionManager,
            tracerProvider.getIfAvailable(() -> Tracer.NOOP),
            relayMetrics,
            maxAttempts
        );
    }

    EventOutboxRelayService(
        LoadEventOutboxPort loadEventOutboxPort,
        SaveEventOutboxPort saveEventOutboxPort,
        EventPayloadDeserializer deserializer,
        ApplicationEventPublisher eventPublisher,
        PlatformTransactionManager transactionManager,
        Tracer tracer,
        int maxAttempts
    ) {
        this(
            loadEventOutboxPort,
            saveEventOutboxPort,
            deserializer,
            eventPublisher,
            transactionManager,
            tracer,
            EventOutboxRelayMetrics.noOp(),
            maxAttempts
        );
    }

    EventOutboxRelayService(
        LoadEventOutboxPort loadEventOutboxPort,
        SaveEventOutboxPort saveEventOutboxPort,
        EventPayloadDeserializer deserializer,
        ApplicationEventPublisher eventPublisher,
        PlatformTransactionManager transactionManager,
        Tracer tracer,
        EventOutboxRelayMetrics relayMetrics,
        int maxAttempts
    ) {
        this.loadEventOutboxPort = loadEventOutboxPort;
        this.saveEventOutboxPort = saveEventOutboxPort;
        this.deserializer = deserializer;
        this.eventPublisher = eventPublisher;
        this.tracer = tracer;
        this.relayMetrics = relayMetrics;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.maxAttempts = maxAttempts;
    }

    /**
     * 한 배치를 선점하고 발행을 시도한다. 반환값은 발행 성공 수가 아닌 선점한 이벤트 수다.
     */
    public int relay(OutboxProcessingGroup group, OutboxEventSelection selection, int limit) {
        Objects.requireNonNull(group);
        Objects.requireNonNull(selection);
        if (limit <= 0) {
            throw new IllegalArgumentException("배치 크기는 양수여야 합니다.");
        }
        long startedAt = System.nanoTime();
        String outcome = "success";
        try {
            List<EventOutbox> outboxes = claimPublishable(selection, limit);
            for (EventOutbox outbox : outboxes) {
                relayOne(outbox, group);
            }
            return outboxes.size();
        } catch (RuntimeException exception) {
            outcome = "failure";
            throw exception;
        } finally {
            if (group != null) {
                relayMetrics.recordDuration("batch", group.name(), outcome, startedAt);
            }
        }
    }

    private List<EventOutbox> claimPublishable(OutboxEventSelection selection, int limit) {
        List<EventOutbox> outboxes = transactionTemplate.execute(status -> {
            Instant now = Instant.now();
            List<EventOutbox> publishableOutboxes = loadEventOutboxPort.listPublishable(selection, limit, now);
            Instant leaseUntil = now.plus(PROCESSING_LEASE);
            publishableOutboxes.forEach(outbox -> outbox.markProcessing(leaseUntil));
            saveEventOutboxPort.saveAll(publishableOutboxes);
            return publishableOutboxes;
        });
        return outboxes == null ? List.of() : outboxes;
    }

    private void relayOne(EventOutbox outbox, OutboxProcessingGroup group) {
        long startedAt = System.nanoTime();
        String outcome = "success";
        try {
            publish(outbox);
        } catch (OptimisticLockingFailureException e) {
            outcome = "ownership_lost";
            logLeaseOwnershipLost(outbox);
        } catch (RuntimeException e) {
            outcome = "failure";
            try {
                recordFailure(outbox, e);
                if (group != null) {
                    relayMetrics.recordGroupFailure(group.name(), outbox.getStatus());
                }
            } catch (OptimisticLockingFailureException ignored) {
                logLeaseOwnershipLost(outbox);
            }
        } finally {
            if (group != null) {
                relayMetrics.recordDuration("publish", group.name(), outcome, startedAt);
            }
        }
    }

    private void logLeaseOwnershipLost(EventOutbox outbox) {
        log.info(
            "Event outbox 처리 소유권이 변경되어 현재 worker의 상태 저장을 생략합니다: eventId={}",
            outbox.getEventId()
        );
    }

    private void publish(EventOutbox outbox) {
        // 발행 시점에 저장해둔 traceparent로 원 요청 trace를 복원해 span link로 연결한다.
        // relay span은 폴러의 task span 아래 자식으로 생성되며, link로 원 요청 trace와 이어진다.
        TraceContext origin = W3CTraceparent.restore(tracer, outbox.getTraceparent());
        Span span = newRelaySpan(origin);
        try (Tracer.SpanInScope ignored = tracer.withSpan(span)) {
            doPublish(outbox);
        } catch (RuntimeException e) {
            ObservabilityErrorSanitizer.record(span, e);
            throw e;
        } finally {
            span.end();
        }
    }

    private Span newRelaySpan(TraceContext origin) {
        Span.Builder builder = tracer.spanBuilder().name(RELAY_SPAN_NAME);
        if (origin != null) {
            builder.addLink(new Link(origin));
        }
        return builder.start();
    }

    private void doPublish(EventOutbox outbox) {
        DomainEvent event = deserializer.deserialize(outbox);
        if (event.outboxDispatchMode() == OutboxDispatchMode.NON_TRANSACTIONAL) {
            eventPublisher.publishEvent(event);
            markPublished(outbox);
            return;
        }

        transactionTemplate.executeWithoutResult(status -> {
            eventPublisher.publishEvent(event);
            outbox.markPublished();
            saveEventOutboxPort.save(outbox);
        });
    }

    private void markPublished(EventOutbox outbox) {
        transactionTemplate.executeWithoutResult(status -> {
            outbox.markPublished();
            saveEventOutboxPort.save(outbox);
        });
    }

    private void recordFailure(EventOutbox outbox, RuntimeException exception) {
        transactionTemplate.executeWithoutResult(status -> {
            outbox.recordFailure(errorMessage(exception), nextAttemptAt(outbox), maxAttempts);
            saveEventOutboxPort.save(outbox);
        });
        relayMetrics.recordFailure(outbox.getStatus());
    }

    private Instant nextAttemptAt(EventOutbox outbox) {
        long multiplier = 1L << Math.min(outbox.getAttempts(), 6);
        Duration backoff = BASE_BACKOFF.multipliedBy(multiplier);
        if (backoff.compareTo(MAX_BACKOFF) > 0) {
            backoff = MAX_BACKOFF;
        }
        return Instant.now().plus(backoff);
    }

    private String errorMessage(RuntimeException exception) {
        if (exception.getMessage() == null || exception.getMessage().isBlank()) {
            return exception.getClass().getName();
        }
        return exception.getMessage();
    }
}
