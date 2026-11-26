package com.umc.product.inquiry.application.port.in.realtime.dto;

import java.time.Instant;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryMessageInfo;
import com.umc.product.inquiry.domain.enums.InquiryStatus;

/**
 * 사람별 큐 {@code /user/queue/inquiries/events}로 보내는 이벤트. 한 주소에 모든 문의와 여러 종류가 오므로
 * {@code inquiryId}와 {@code type}으로 구분한다.
 */
public sealed interface InquiryRealtimeEvent {

    @JsonProperty("type")
    InquiryRealtimeEventType type();

    Long inquiryId();

    @JsonPropertyOrder({"type", "inquiryId", "message"})
    record MessageCreated(Long inquiryId, InquiryMessageInfo message) implements InquiryRealtimeEvent {

        public MessageCreated {
            Objects.requireNonNull(inquiryId, "inquiryId");
            Objects.requireNonNull(message, "message");
        }

        @Override
        @JsonProperty("type")
        public InquiryRealtimeEventType type() {
            return InquiryRealtimeEventType.MESSAGE_CREATED;
        }
    }

    @JsonPropertyOrder({"type", "inquiryId", "status", "closedAt", "lastMessageAt", "occurredAt"})
    record StatusChanged(
        Long inquiryId,
        InquiryStatus status,
        Instant closedAt,
        Instant lastMessageAt,
        Instant occurredAt
    ) implements InquiryRealtimeEvent {

        public StatusChanged {
            Objects.requireNonNull(inquiryId, "inquiryId");
            Objects.requireNonNull(status, "status");
            Objects.requireNonNull(lastMessageAt, "lastMessageAt");
            Objects.requireNonNull(occurredAt, "occurredAt");
        }

        @Override
        @JsonProperty("type")
        public InquiryRealtimeEventType type() {
            return InquiryRealtimeEventType.STATUS_CHANGED;
        }
    }
}
