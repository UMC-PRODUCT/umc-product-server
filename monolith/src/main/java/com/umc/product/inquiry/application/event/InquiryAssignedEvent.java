package com.umc.product.inquiry.application.event;

import java.time.Instant;
import java.util.UUID;

import com.umc.product.global.event.domain.DomainEvent;

/**
 * 다른 운영진이 담당자로 지정했을 때만 남긴다. 스스로 맡으면 남기지 않는다.
 */
public record InquiryAssignedEvent(
    UUID eventId,
    Instant occurredAt,
    Long inquiryId,
    Long assigneeMemberId,
    Long actorMemberId,
    String title
) implements DomainEvent {

    public InquiryAssignedEvent {
        eventId = eventId == null ? UUID.randomUUID() : eventId;
        occurredAt = occurredAt == null ? Instant.now() : occurredAt;
    }

    public static InquiryAssignedEvent of(Long inquiryId, Long assigneeMemberId, Long actorMemberId, String title) {
        return new InquiryAssignedEvent(null, null, inquiryId, assigneeMemberId, actorMemberId, title);
    }

    @Override
    public String eventType() {
        return "inquiry.assigned";
    }
}
