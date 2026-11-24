package com.umc.product.inquiry.application.event;

import java.time.Instant;
import java.util.UUID;

import com.umc.product.global.event.domain.DomainEvent;

/**
 * 운영진이 문의에 새 메시지를 남겼다는 사실. 알림 consumer 가 작성자에게 푸시를 보낼 때 쓴다.
 */
public record InquiryStaffRepliedEvent(
    UUID eventId,
    Instant occurredAt,
    Long inquiryId,
    Long messageId,
    Long authorMemberId,
    Long staffMemberId
) implements DomainEvent {

    public InquiryStaffRepliedEvent {
        eventId = eventId == null ? UUID.randomUUID() : eventId;
        occurredAt = occurredAt == null ? Instant.now() : occurredAt;
    }

    public static InquiryStaffRepliedEvent of(
        Long inquiryId,
        Long messageId,
        Long authorMemberId,
        Long staffMemberId
    ) {
        return new InquiryStaffRepliedEvent(null, null, inquiryId, messageId, authorMemberId, staffMemberId);
    }

    @Override
    public String eventType() {
        return "inquiry.staff.replied";
    }
}
