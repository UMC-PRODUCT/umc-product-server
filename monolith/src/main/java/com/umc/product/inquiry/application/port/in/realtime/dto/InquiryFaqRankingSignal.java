package com.umc.product.inquiry.application.port.in.realtime.dto;

import java.time.Instant;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.umc.product.inquiry.domain.enums.InquiryFaqRankingPeriod;

/**
 * 공용 주소 {@code /topic/inquiries/ranking}으로 보내는 "새 순위 있음" 신호. 개인정보 없이 기간과 계산 시각만 담는다.
 */
@JsonPropertyOrder({"type", "period", "refreshedAt"})
public record InquiryFaqRankingSignal(InquiryFaqRankingPeriod period, Instant refreshedAt) {

    public static final String TYPE = "RANKING_UPDATED";

    public InquiryFaqRankingSignal {
        Objects.requireNonNull(period, "period");
        Objects.requireNonNull(refreshedAt, "refreshedAt");
    }

    @JsonProperty("type")
    public String type() {
        return TYPE;
    }
}
