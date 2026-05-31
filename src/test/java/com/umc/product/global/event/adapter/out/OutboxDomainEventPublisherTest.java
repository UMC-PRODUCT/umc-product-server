package com.umc.product.global.event.adapter.out;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.product.global.event.application.port.out.SaveEventOutboxPort;
import com.umc.product.global.event.domain.DomainEvent;
import com.umc.product.global.event.domain.EventOutbox;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("OutboxDomainEventPublisher")
class OutboxDomainEventPublisherTest {

    @Test
    @DisplayName("publish는 도메인 이벤트를 직발행하지 않고 event outbox로 저장한다")
    void publish_저장() {
        FakeSaveEventOutboxPort savePort = new FakeSaveEventOutboxPort();
        OutboxDomainEventPublisher publisher = new OutboxDomainEventPublisher(
            savePort,
            new EventPayloadSerializer(new ObjectMapper().findAndRegisterModules())
        );
        TestEvent event = TestEvent.create("test.created", "hello");

        publisher.publish(event);

        assertThat(savePort.saved).hasSize(1);
        EventOutbox outbox = savePort.saved.getFirst();
        assertThat(outbox.getEventId()).isEqualTo(event.eventId());
        assertThat(outbox.getEventType()).isEqualTo("test.created");
        assertThat(outbox.getPayload()).contains("\"message\":\"hello\"");
    }

    @Test
    @DisplayName("publishAll은 입력 순서대로 모든 이벤트를 일괄 저장한다")
    void publishAll_저장() {
        FakeSaveEventOutboxPort savePort = new FakeSaveEventOutboxPort();
        OutboxDomainEventPublisher publisher = new OutboxDomainEventPublisher(
            savePort,
            new EventPayloadSerializer(new ObjectMapper().findAndRegisterModules())
        );
        TestEvent first = TestEvent.create("test.first", "first");
        TestEvent second = TestEvent.create("test.second", "second");

        publisher.publishAll(List.of(first, second));

        assertThat(savePort.saved)
            .extracting(EventOutbox::getEventId)
            .containsExactly(first.eventId(), second.eventId());
        assertThat(savePort.saveAllCalled).isTrue();
    }

    private static class FakeSaveEventOutboxPort implements SaveEventOutboxPort {

        private final List<EventOutbox> saved = new ArrayList<>();

        @Override
        public void save(EventOutbox eventOutbox) {
            saved.add(eventOutbox);
        }

        @Override
        public void saveAll(Collection<EventOutbox> eventOutboxes) {
            saveAllCalled = true;
            saved.addAll(eventOutboxes);
        }

        private boolean saveAllCalled;
    }

    private record TestEvent(
        UUID eventId,
        Instant occurredAt,
        String eventType,
        String message
    ) implements DomainEvent {

        static TestEvent create(String eventType, String message) {
            return new TestEvent(UUID.randomUUID(), Instant.now(), eventType, message);
        }
    }
}
