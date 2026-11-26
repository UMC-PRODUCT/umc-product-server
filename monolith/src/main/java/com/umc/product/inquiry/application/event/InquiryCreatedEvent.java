package com.umc.product.inquiry.application.event;

import java.time.Instant;
import java.util.UUID;

import com.umc.product.global.event.domain.DomainEvent;

public record InquiryCreatedEvent(
    UUID eventId,
    Instant occurredAt,
    Long inquiryId,
    Long authorMemberId,
    String title
) implements DomainEvent {

    public InquiryCreatedEvent {
        eventId = eventId == null ? UUID.randomUUID() : eventId;
        occurredAt = occurredAt == null ? Instant.now() : occurredAt;
    }

    public static InquiryCreatedEvent of(Long inquiryId, Long authorMemberId, String title) {
        return new InquiryCreatedEvent(null, null, inquiryId, authorMemberId, title);
    }

    @Override
    public String eventType() {
        return "inquiry.created";
    }
}
