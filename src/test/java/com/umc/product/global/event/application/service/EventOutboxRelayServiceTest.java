package com.umc.product.global.event.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.product.global.event.adapter.out.EventPayloadDeserializer;
import com.umc.product.global.event.adapter.out.EventPayloadSerializer;
import com.umc.product.global.event.application.port.out.LoadEventOutboxPort;
import com.umc.product.global.event.application.port.out.SaveEventOutboxPort;
import com.umc.product.global.event.domain.DomainEvent;
import com.umc.product.global.event.domain.EventOutbox;
import com.umc.product.global.event.domain.EventOutboxStatus;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

@DisplayName("EventOutboxRelayService")
class EventOutboxRelayServiceTest {

    @Test
    @DisplayName("publishable outbox를 DomainEvent로 복원해 Spring event bus로 발행하고 published 처리한다")
    void relay_성공() {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        EventPayloadSerializer serializer = new EventPayloadSerializer(objectMapper);
        TestEvent event = TestEvent.create("test.created", "hello");
        EventOutbox outbox = EventOutbox.record(event, serializer.serialize(event));
        FakeLoadEventOutboxPort loadPort = new FakeLoadEventOutboxPort(List.of(outbox));
        FakeSaveEventOutboxPort savePort = new FakeSaveEventOutboxPort();
        CapturingApplicationEventPublisher publisher = new CapturingApplicationEventPublisher();
        EventOutboxRelayService relayService = new EventOutboxRelayService(
            loadPort,
            savePort,
            new EventPayloadDeserializer(objectMapper),
            publisher,
            new LocalTransactionManager(),
            100,
            3
        );

        relayService.relay();

        assertThat(outbox.getStatus()).isEqualTo(EventOutboxStatus.PUBLISHED);
        assertThat(publisher.events).hasSize(1);
        assertThat(publisher.events.getFirst()).isInstanceOf(TestEvent.class);
        assertThat(((TestEvent) publisher.events.getFirst()).message()).isEqualTo("hello");
        assertThat(savePort.savedStatuses).contains(EventOutboxStatus.PROCESSING, EventOutboxStatus.PUBLISHED);
    }

    @Test
    @DisplayName("이벤트 복원 또는 발행 실패 시 별도 상태 저장 트랜잭션에서 attempts를 증가시키고 pending으로 남긴다")
    void relay_실패_재시도() {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        EventPayloadSerializer serializer = new EventPayloadSerializer(objectMapper);
        TestEvent event = TestEvent.create("test.created", "hello");
        EventOutbox outbox = EventOutbox.record(event, serializer.serialize(event));
        FakeLoadEventOutboxPort loadPort = new FakeLoadEventOutboxPort(List.of(outbox));
        FakeSaveEventOutboxPort savePort = new FakeSaveEventOutboxPort();
        ApplicationEventPublisher publisher = ignored -> {
            throw new IllegalStateException("publish failed");
        };
        EventOutboxRelayService relayService = new EventOutboxRelayService(
            loadPort,
            savePort,
            new EventPayloadDeserializer(objectMapper),
            publisher,
            new LocalTransactionManager(),
            100,
            3
        );

        relayService.relay();

        assertThat(outbox.getStatus()).isEqualTo(EventOutboxStatus.PENDING);
        assertThat(outbox.getAttempts()).isEqualTo(1);
        assertThat(outbox.getLastError()).contains("publish failed");
        assertThat(savePort.savedStatuses).contains(EventOutboxStatus.PROCESSING, EventOutboxStatus.PENDING);
    }

    @Test
    @DisplayName("최대 재시도 횟수에 도달하면 failed 상태로 저장한다")
    void relay_최대_재시도_도달() {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        EventPayloadSerializer serializer = new EventPayloadSerializer(objectMapper);
        TestEvent event = TestEvent.create("test.created", "hello");
        EventOutbox outbox = EventOutbox.record(event, serializer.serialize(event));
        outbox.recordFailure("previous", Instant.now(), 2);
        FakeLoadEventOutboxPort loadPort = new FakeLoadEventOutboxPort(List.of(outbox));
        FakeSaveEventOutboxPort savePort = new FakeSaveEventOutboxPort();
        ApplicationEventPublisher publisher = ignored -> {
            throw new IllegalStateException("publish failed");
        };
        EventOutboxRelayService relayService = new EventOutboxRelayService(
            loadPort,
            savePort,
            new EventPayloadDeserializer(objectMapper),
            publisher,
            new LocalTransactionManager(),
            100,
            2
        );

        relayService.relay();

        assertThat(outbox.getStatus()).isEqualTo(EventOutboxStatus.FAILED);
        assertThat(outbox.getAttempts()).isEqualTo(2);
        assertThat(savePort.savedStatuses).contains(EventOutboxStatus.PROCESSING, EventOutboxStatus.FAILED);
    }

    private static class FakeLoadEventOutboxPort implements LoadEventOutboxPort {

        private final List<EventOutbox> outboxes;

        private FakeLoadEventOutboxPort(List<EventOutbox> outboxes) {
            this.outboxes = outboxes;
        }

        @Override
        public List<EventOutbox> listPublishable(int limit, Instant now) {
            return outboxes;
        }
    }

    private static class FakeSaveEventOutboxPort implements SaveEventOutboxPort {

        private final List<EventOutboxStatus> savedStatuses = new ArrayList<>();

        @Override
        public void save(EventOutbox eventOutbox) {
            savedStatuses.add(eventOutbox.getStatus());
        }

        @Override
        public void saveAll(Collection<EventOutbox> eventOutboxes) {
            eventOutboxes.forEach(eventOutbox -> savedStatuses.add(eventOutbox.getStatus()));
        }
    }

    private static class CapturingApplicationEventPublisher implements ApplicationEventPublisher {

        private final List<Object> events = new ArrayList<>();

        @Override
        public void publishEvent(Object event) {
            events.add(event);
        }
    }

    public record TestEvent(
        UUID eventId,
        Instant occurredAt,
        String eventType,
        String message
    ) implements DomainEvent {

        static TestEvent create(String eventType, String message) {
            return new TestEvent(UUID.randomUUID(), Instant.now(), eventType, message);
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

        @Override
        protected boolean isExistingTransaction(Object transaction) throws TransactionException {
            return false;
        }
    }
}
