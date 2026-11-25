package com.umc.product.inquiry.application.event;

import java.time.Instant;
import java.util.UUID;

import com.umc.product.global.event.domain.DomainEvent;
import com.umc.product.global.event.domain.OutboxDispatchMode;
import com.umc.product.inquiry.domain.Inquiry;
import com.umc.product.inquiry.domain.enums.InquiryStatus;

/**
 * 문의 상태가 바뀐 뒤의 스냅숏. relay 시점에 문의를 다시 읽지 않으므로 그 사이 바뀐 상태가 섞이지 않는다.
 */
public record InquiryStatusChangedEvent(
    UUID eventId,
    Instant occurredAt,
    Long inquiryId,
    Long authorMemberId,
    InquiryStatus status,
    Instant closedAt,
    Instant lastMessageAt
) implements DomainEvent {

    public InquiryStatusChangedEvent {
        eventId = eventId == null ? UUID.randomUUID() : eventId;
        occurredAt = occurredAt == null ? Instant.now() : occurredAt;
    }

    public static InquiryStatusChangedEvent from(Inquiry inquiry) {
        return new InquiryStatusChangedEvent(
            null,
            null,
            inquiry.getId(),
            inquiry.getAuthorMemberId(),
            inquiry.getStatus(),
            inquiry.getClosedAt(),
            inquiry.getLastMessageAt()
        );
    }

    @Override
    public String eventType() {
        return "inquiry.status.changed";
    }

    @Override
    public OutboxDispatchMode outboxDispatchMode() {
        return OutboxDispatchMode.NON_TRANSACTIONAL;
    }
}
