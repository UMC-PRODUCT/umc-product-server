package com.umc.product.inquiry.application.port.in.query.dto;

import java.time.Instant;
import java.util.List;

import com.umc.product.inquiry.domain.enums.InquiryFaqRankingPeriod;

/**
 * @param refreshedAt 순위를 계산한 시각
 */
public record InquiryFaqRankingInfo(
    InquiryFaqRankingPeriod period,
    Instant refreshedAt,
    List<Item> items
) {
    /**
     * @param askedMemberCount 기간 안에 이 대표 질문에 연결된 문의를 남긴 서로 다른 회원 수
     */
    public record Item(
        int rank,
        InquiryFaqSummaryInfo faq,
        long askedMemberCount
    ) {
    }
}
