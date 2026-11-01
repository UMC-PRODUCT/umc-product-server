package com.umc.product.global.event.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.product.global.config.OutboxRoutingConfig;
import com.umc.product.global.event.adapter.out.EventPayloadDeserializer;
import com.umc.product.global.event.adapter.out.EventPayloadSerializer;
import com.umc.product.global.event.application.port.out.SaveEventOutboxPort;
import com.umc.product.global.event.domain.DomainEvent;
import com.umc.product.global.event.domain.EventOutbox;
import com.umc.product.global.event.domain.EventOutboxStatus;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.tracing.Tracer;

@DisplayName("EventOutbox relay 실패 metric")
class EventOutboxRelayMetricsTest {

    @Test
    @DisplayName("발행 실패가 재시도 상태로 저장되면 retry counter만 증가한다")
    void retryFailureIncrementsRetryCounter() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        EventOutbox outbox = outbox();
        EventOutboxRelayService sut = relayService(outbox, meterRegistry, 2);

        sut.relay(
            OutboxProcessingGroup.BACKGROUND,
            new OutboxRoutingConfig().outboxEventRoutingPolicy().selectionFor(OutboxProcessingGroup.BACKGROUND),
            100
        );

        assertThat(outbox.getStatus()).isEqualTo(EventOutboxStatus.PENDING);
        assertThat(meterRegistry.get("event.outbox.relay.retry").counter().count()).isEqualTo(1.0);
        assertThat(meterRegistry.get("event.outbox.relay.retry").counter().getId().getTags()).isEmpty();
        assertThat(meterRegistry.get("event.outbox.relay.failed").counter().count()).isZero();
    }

    @Test
    @DisplayName("발행 실패가 최대 시도 횟수에 도달하면 FAILED counter만 증가한다")
    void terminalFailureIncrementsFailedCounter() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        EventOutbox outbox = outbox();
        outbox.recordFailure("previous failure", Instant.now(), 2);
        EventOutboxRelayService sut = relayService(outbox, meterRegistry, 2);

        sut.relay(
            OutboxProcessingGroup.BACKGROUND,
            new OutboxRoutingConfig().outboxEventRoutingPolicy().selectionFor(OutboxProcessingGroup.BACKGROUND),
            100
        );

        assertThat(outbox.getStatus()).isEqualTo(EventOutboxStatus.FAILED);
        assertThat(meterRegistry.get("event.outbox.relay.retry").counter().count()).isZero();
        assertThat(meterRegistry.get("event.outbox.relay.failed").counter().count()).isEqualTo(1.0);
        assertThat(meterRegistry.get("event.outbox.relay.failed").counter().getId().getTags()).isEmpty();
    }

    @Test
    @DisplayName("그룹 실행은 발행 시간과 재시도 결과를 해당 그룹에 기록한다")
    void 그룹별_발행_관측() {
        var registry = new SimpleMeterRegistry();
        EventOutbox outbox = outbox();
        var sut = relayService(outbox, registry, 2);
        var selection = new com.umc.product.global.config.OutboxRoutingConfig()
            .outboxEventRoutingPolicy().selectionFor(OutboxProcessingGroup.BACKGROUND);

        sut.relay(OutboxProcessingGroup.BACKGROUND, selection, 10);

        assertThat(registry.get("event.outbox.publish.duration").tags("group", "background", "outcome", "failure")
            .timer().count()).isEqualTo(1);
        assertThat(registry.get("event.outbox.group.failure").tags("group", "background", "outcome", "retry")
            .counter().count()).isEqualTo(1);
        assertThat(registry.get("event.outbox.batch.duration").tags("group", "background", "outcome", "success")
            .timer().count()).isEqualTo(1);
    }

    @Test
    @DisplayName("executor 거절은 해당 그룹의 거절 횟수로 기록한다")
    void 그룹별_거절_관측() {
        var registry = new SimpleMeterRegistry();
        var metrics = new EventOutboxRelayMetrics(registry);
        var policy = new com.umc.product.global.config.OutboxRoutingConfig().outboxEventRoutingPolicy();
        var worker = new OutboxWorker(OutboxProcessingGroup.REALTIME, task -> {
            throw new java.util.concurrent.RejectedExecutionException();
        }, relayService(outbox(), registry, 2), policy, 10, metrics);

        assertThat(worker.requestRun()).isFalse();

        assertThat(registry.get("event.outbox.worker.rejected").tag("group", "realtime")
            .counter().count()).isEqualTo(1);
    }

    private EventOutboxRelayService relayService(
        EventOutbox outbox,
        SimpleMeterRegistry meterRegistry,
        int maxAttempts
    ) {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        return new EventOutboxRelayService(
            new com.umc.product.global.event.application.port.out.LoadEventOutboxPort() {

                @Override
                public List<EventOutbox> listPublishable(
                    com.umc.product.global.event.application.port.out.OutboxEventSelection selection,
                    int limit, Instant now
                ) {
                    return List.of(outbox);
                }
            },
            new NoOpSaveEventOutboxPort(),
            new EventPayloadDeserializer(objectMapper),
            ignored -> {
                throw new IllegalStateException("publish failed");
            },
            new LocalTransactionManager(),
            Tracer.NOOP,
            new EventOutboxRelayMetrics(meterRegistry),
            maxAttempts
        );
    }

    private EventOutbox outbox() {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        TestEvent event = new TestEvent(UUID.randomUUID(), Instant.now());
        String payload = new EventPayloadSerializer(objectMapper).serialize(event);
        return EventOutbox.record(event, payload);
    }

    private record TestEvent(UUID eventId, Instant occurredAt) implements DomainEvent {

        @Override
        public String eventType() {
            return "test.created";
        }
    }

    private static class NoOpSaveEventOutboxPort implements SaveEventOutboxPort {

        @Override
        public void save(EventOutbox eventOutbox) {
        }

        @Override
        public void saveAll(Collection<EventOutbox> eventOutboxes) {
        }
    }

    private static class LocalTransactionManager extends AbstractPlatformTransactionManager {

        @Override
        protected Object doGetTransaction() throws TransactionException {
            return new Object();
        }

        @Override
        protected void doBegin(Object transaction, TransactionDefinition definition) throws TransactionException {
        }

        @Override
        protected void doCommit(DefaultTransactionStatus status) throws TransactionException {
        }

        @Override
        protected void doRollback(DefaultTransactionStatus status) throws TransactionException {
        }
    }
}
