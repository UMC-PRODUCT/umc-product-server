package com.umc.product.global.event.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.umc.product.global.event.domain.DomainEvent;
import com.umc.product.global.event.domain.EventOutbox;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("EventOutboxPersistenceAdapter")
@ExtendWith(MockitoExtension.class)
class EventOutboxPersistenceAdapterTest {

    @Mock
    private EventOutboxJpaRepository repository;

    @Test
    @DisplayName("save는 repository save로 위임한다")
    void save() {
        EventOutboxPersistenceAdapter adapter = new EventOutboxPersistenceAdapter(repository);
        EventOutbox outbox = newOutbox();

        adapter.save(outbox);

        verify(repository).save(outbox);
    }

    @Test
    @DisplayName("saveAll은 repository saveAll로 위임한다")
    void saveAll() {
        EventOutboxPersistenceAdapter adapter = new EventOutboxPersistenceAdapter(repository);
        List<EventOutbox> outboxes = List.of(newOutbox(), newOutbox());

        adapter.saveAll(outboxes);

        verify(repository).saveAll(outboxes);
    }

    @Test
    @DisplayName("listPublishable은 repository의 lock 조회로 위임한다")
    void listPublishable() {
        EventOutboxPersistenceAdapter adapter = new EventOutboxPersistenceAdapter(repository);
        Instant now = Instant.parse("2026-05-21T00:00:00Z");
        List<EventOutbox> expected = List.of(newOutbox());
        given(repository.findPublishableForUpdate(100, now)).willReturn(expected);

        List<EventOutbox> result = adapter.listPublishable(100, now);

        assertThat(result).isSameAs(expected);
    }

    private EventOutbox newOutbox() {
        return EventOutbox.record(TestEvent.create("test.created"), "{}");
    }

    private record TestEvent(
        UUID eventId,
        Instant occurredAt,
        String eventType
    ) implements DomainEvent {

        static TestEvent create(String eventType) {
            return new TestEvent(UUID.randomUUID(), Instant.now(), eventType);
        }
    }
}
