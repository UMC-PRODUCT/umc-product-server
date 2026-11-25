package com.umc.product.inquiry.domain.enums;

import java.time.Duration;
import java.time.Instant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 인기 순위를 세는 기간. 문의 접수 시각이 지금부터 이 기간 안에 있는 문의만 센다.
 */
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
