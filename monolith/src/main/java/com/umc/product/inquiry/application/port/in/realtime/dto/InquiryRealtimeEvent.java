package com.umc.product.inquiry.application.port.in.realtime.dto;

import java.time.Instant;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryMessageInfo;
import com.umc.product.inquiry.domain.enums.InquiryStatus;

/**
 * {@code /topic/inquiries/{inquiryId}}로 보내는 이벤트. 한 주소에 여러 종류가 오므로 {@code type}으로 구분한다.
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

    @JsonPropertyOrder({"type", "inquiryId", "status", "closedAt", "lastMessageAt"})
    record StatusChanged(
        Long inquiryId,
        InquiryStatus status,
        Instant closedAt,
        Instant lastMessageAt
    ) implements InquiryRealtimeEvent {

        public StatusChanged {
            Objects.requireNonNull(inquiryId, "inquiryId");
            Objects.requireNonNull(status, "status");
            Objects.requireNonNull(lastMessageAt, "lastMessageAt");
        }

        @Override
        @JsonProperty("type")
        public InquiryRealtimeEventType type() {
            return InquiryRealtimeEventType.STATUS_CHANGED;
        }
    }
}
