package com.umc.product.global.event.adapter.out;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.product.global.event.domain.DomainEvent;
import com.umc.product.global.event.domain.EventOutbox;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventPayloadDeserializer {

    private final ObjectMapper objectMapper;

    public DomainEvent deserialize(EventOutbox eventOutbox) {
        Class<? extends DomainEvent> eventClass = resolveEventClass(eventOutbox.getEventClass());
        try {
            return objectMapper.readValue(eventOutbox.getPayload(), eventClass);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                "event outbox payload 역직렬화에 실패했습니다. eventType=" + eventOutbox.getEventType(),
                e
            );
        }
    }

    private Class<? extends DomainEvent> resolveEventClass(String eventClassName) {
        try {
            Class<?> rawClass = Class.forName(eventClassName);
            if (!DomainEvent.class.isAssignableFrom(rawClass)) {
                throw new IllegalStateException("DomainEvent 타입이 아닙니다. eventClass=" + eventClassName);
            }
            return rawClass.asSubclass(DomainEvent.class);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("event class를 찾을 수 없습니다. eventClass=" + eventClassName, e);
        }
    }
}
