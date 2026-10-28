package com.umc.product.report.domain.event;

import java.time.Instant;
import java.util.UUID;

import com.umc.product.global.event.domain.DomainEvent;
import com.umc.product.global.event.domain.OutboxDispatchMode;

/**
 * 신고가 접수되었음을 알리는 이벤트입니다. 운영 채널 전달에 사용합니다.
 * <p>
 * 전달은 외부 HTTP 호출이므로 relay 트랜잭션 밖에서 처리합니다.
 */
public record ReportSubmittedEvent(
    UUID eventId,
    Instant occurredAt,
    Long reportId
) implements DomainEvent {

    public ReportSubmittedEvent {
        if (eventId == null) {
            eventId = UUID.randomUUID();
        }
        if (occurredAt == null) {
            occurredAt = Instant.now();
        }
        if (reportId == null || reportId <= 0) {
            throw new IllegalArgumentException("reportId must be positive");
        }
    }

    public static ReportSubmittedEvent of(Long reportId) {
        return new ReportSubmittedEvent(null, null, reportId);
    }

    @Override
    public String eventType() {
        return "report.submitted";
    }

    @Override
    public OutboxDispatchMode outboxDispatchMode() {
        return OutboxDispatchMode.NON_TRANSACTIONAL;
    }
}
