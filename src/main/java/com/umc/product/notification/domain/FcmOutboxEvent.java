package com.umc.product.notification.domain;

import com.umc.product.global.event.domain.DomainEvent;
import java.time.Instant;
import java.util.UUID;

/**
 * FCM outbox에 신규 항목이 적재되었음을 알리는 트리거 이벤트.
 * <p>
 * 페이로드는 의도적으로 비워둔다. 수신자({@code FcmOutboxEventListener})는 이벤트 자체를
 * 트리거로만 사용하고, 실제 outbox 항목은 polling으로 직접 읽어 처리한다.
 */
public record FcmOutboxEvent(UUID eventId, Instant occurredAt) implements DomainEvent {

    public static FcmOutboxEvent create() {
        return new FcmOutboxEvent(UUID.randomUUID(), Instant.now());
    }

    @Override
    public String eventType() {
        return "fcm.outbox.created";
    }
}
