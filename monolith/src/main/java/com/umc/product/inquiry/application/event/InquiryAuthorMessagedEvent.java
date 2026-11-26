package com.umc.product.inquiry.application.event;

import java.time.Instant;
import java.util.UUID;

import com.umc.product.global.event.domain.DomainEvent;

/**
 * 작성자 쪽에서 보낸 중복 아닌 메시지. 운영진이 자기 문의에 쓴 메시지도 여기에 든다.
 */
public record InquiryAuthorMessagedEvent(
    UUID eventId,
    Instant occurredAt,
    Long inquiryId,
    Long messageId,
    Long authorMemberId,
    String title
) implements DomainEvent {

    public InquiryAuthorMessagedEvent {
        eventId = eventId == null ? UUID.randomUUID() : eventId;
        occurredAt = occurredAt == null ? Instant.now() : occurredAt;
    }

    public static InquiryAuthorMessagedEvent of(Long inquiryId, Long messageId, Long authorMemberId, String title) {
        return new InquiryAuthorMessagedEvent(null, null, inquiryId, messageId, authorMemberId, title);
    }

    @Override
    public String eventType() {
        return "inquiry.author.messaged";
    }
}
