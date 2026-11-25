package com.umc.product.inquiry.domain.enums;

import java.time.Duration;
import java.time.Instant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InquiryFaqRankingPeriod {
    DAY(Duration.ofHours(24)),
    WEEK(Duration.ofDays(7));

    private final Duration window;

    public Instant since(Instant now) {
        return now.minus(window);
    }
}
