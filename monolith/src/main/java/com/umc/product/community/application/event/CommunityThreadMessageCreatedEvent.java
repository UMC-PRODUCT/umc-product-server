package com.umc.product.community.application.event;

import java.time.Instant;
import java.util.UUID;

import com.umc.product.global.event.domain.DomainEvent;

public record CommunityThreadMessageCreatedEvent(
    UUID eventId,
    Instant occurredAt,
    Long threadId,
    Long messageId,
    Long senderMemberId
) implements DomainEvent {

    public CommunityThreadMessageCreatedEvent {
        eventId = eventId == null ? UUID.randomUUID() : eventId;
        occurredAt = occurredAt == null ? Instant.now() : occurredAt;
        threadId = requirePositive(threadId, "threadId");
        messageId = requirePositive(messageId, "messageId");
        senderMemberId = requirePositive(senderMemberId, "senderMemberId");
    }

    public static CommunityThreadMessageCreatedEvent of(
        Long threadId,
        Long messageId,
        Long senderMemberId
    ) {
        return new CommunityThreadMessageCreatedEvent(
            null,
            null,
            threadId,
            messageId,
            senderMemberId
        );
    }

    @Override
    public String eventType() {
        return "community.thread.message.created";
    }

    private static Long requirePositive(Long value, String fieldName) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive");
        }
        return value;
    }
}
