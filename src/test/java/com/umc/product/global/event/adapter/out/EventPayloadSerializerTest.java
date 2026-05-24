package com.umc.product.global.event.adapter.out;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.product.global.event.domain.DomainEvent;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("EventPayloadSerializer")
class EventPayloadSerializerTest {

    @Test
    @DisplayName("도메인 이벤트를 JSON payload로 직렬화한다")
    void serialize() {
        EventPayloadSerializer serializer = new EventPayloadSerializer(new ObjectMapper().findAndRegisterModules());
        TestEvent event = TestEvent.create("test.created", "hello");

        String payload = serializer.serialize(event);

        assertThat(payload).contains("\"eventType\":\"test.created\"");
        assertThat(payload).contains("\"message\":\"hello\"");
    }

    @Test
    @DisplayName("직렬화 실패 시 eventType을 포함한 예외를 던진다")
    void serialize_실패() throws Exception {
        ObjectMapper objectMapper = mock(ObjectMapper.class);
        EventPayloadSerializer serializer = new EventPayloadSerializer(objectMapper);
        TestEvent event = TestEvent.create("test.created", "hello");
        given(objectMapper.writeValueAsString(event))
            .willThrow(new JsonMappingException(null, "boom"));

        assertThatThrownBy(() -> serializer.serialize(event))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("eventType=test.created");
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
