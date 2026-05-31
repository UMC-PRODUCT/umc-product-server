package com.umc.product.notification.domain;

import com.umc.product.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "fcm_outbox")
public class FcmOutbox extends BaseEntity {

    private static final int MAX_RETRY = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private FcmOutboxEventType eventType;

    @Column(name = "payload")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private FcmOutboxStatus status;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "processed_at")
    private Instant processedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private FcmOutbox(Long memberId, FcmOutboxEventType eventType, String payload) {
        this.memberId = memberId;
        this.eventType = eventType;
        this.payload = payload;
        this.status = FcmOutboxStatus.PENDING;
        this.retryCount = 0;
    }

    public static FcmOutbox subscribeEvent(Long memberId) {
        return FcmOutbox.builder()
                .memberId(memberId)
                .eventType(FcmOutboxEventType.FCM_SUBSCRIBE)
                .build();
    }

    public static FcmOutbox unsubscribeEvent(Long memberId, String oldToken) {
        return FcmOutbox.builder()
                .memberId(memberId)
                .eventType(FcmOutboxEventType.FCM_UNSUBSCRIBE)
                .payload(oldToken)
                .build();
    }

    public void markProcessed() {
        this.status = FcmOutboxStatus.PROCESSED;
        this.processedAt = Instant.now();
    }

    public void markFailed() {
        this.status = FcmOutboxStatus.FAILED;
    }

    public void incrementRetry() {
        this.retryCount++;
        if (this.retryCount >= MAX_RETRY) {
            this.status = FcmOutboxStatus.FAILED;
        }
    }
}
