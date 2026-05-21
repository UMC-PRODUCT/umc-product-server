package com.umc.product.global.event.adapter.out;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.product.global.event.domain.DomainEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventPayloadSerializer {

    private final ObjectMapper objectMapper;

    public String serialize(DomainEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("도메인 이벤트 직렬화에 실패했습니다. eventType=" + event.eventType(), e);
        }
    }
}
