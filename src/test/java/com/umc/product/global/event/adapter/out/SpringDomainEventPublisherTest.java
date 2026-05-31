package com.umc.product.global.event.adapter.out;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.umc.product.global.event.domain.DomainEvent;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class SpringDomainEventPublisherTest {

    @Mock
    private ApplicationEventPublisher delegate;

    @InjectMocks
    private SpringDomainEventPublisher publisher;

    @Test
    @DisplayName("publish는 ApplicationEventPublisher로 위임된다")
    void publish_위임_성공() {
        // given
        DomainEvent event = new TestEvent(UUID.randomUUID(), Instant.now(), "test.created");

        // when
        publisher.publish(event);

        // then
        verify(delegate, times(1)).publishEvent(event);
        verifyNoMoreInteractions(delegate);
    }

    @Test
    @DisplayName("publishAll은 입력 컬렉션 순서대로 모든 이벤트를 위임한다")
    void publishAll_순서대로_위임_성공() {
        // given
        DomainEvent first = new TestEvent(UUID.randomUUID(), Instant.now(), "test.first");
        DomainEvent second = new TestEvent(UUID.randomUUID(), Instant.now(), "test.second");

        // when
        publisher.publishAll(List.of(first, second));

        // then
        verify(delegate).publishEvent(first);
        verify(delegate).publishEvent(second);
        verifyNoMoreInteractions(delegate);
    }

    @Test
    @DisplayName("publishAll에 빈 컬렉션이 주어지면 위임 없이 정상 종료된다")
    void publishAll_빈_컬렉션_정상_종료() {
        // when
        publisher.publishAll(List.of());

        // then
        verifyNoMoreInteractions(delegate);
    }

    private record TestEvent(UUID eventId, Instant occurredAt, String eventType)
        implements DomainEvent {
    }
}
