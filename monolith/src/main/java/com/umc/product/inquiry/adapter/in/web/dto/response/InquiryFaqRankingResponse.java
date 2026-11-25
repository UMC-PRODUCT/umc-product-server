package com.umc.product.inquiry.adapter.in.web.dto.response;

import java.time.Instant;
import java.util.List;

import com.umc.product.inquiry.application.port.in.query.dto.InquiryFaqRankingInfo;
import com.umc.product.inquiry.domain.enums.InquiryFaqRankingPeriod;

import io.swagger.v3.oas.annotations.media.Schema;

public record InquiryFaqRankingResponse(
    InquiryFaqRankingPeriod period,
    @Schema(description = "순위를 계산한 시각")
    Instant refreshedAt,
    List<Item> items
) {
    public static InquiryFaqRankingResponse from(InquiryFaqRankingInfo info) {
        return new InquiryFaqRankingResponse(
            info.period(),
            info.refreshedAt(),
            info.items().stream()
                .map(item -> new Item(
                    item.rank(),
                    item.faq().faqId(),
                    item.faq().question(),
                    InquiryCategoryResponse.from(item.faq().category()),
                    InquiryFaqAudienceResponse.from(item.faq().audience()),
                    item.askedMemberCount()
                ))
                .toList()
        );
    }

    public record Item(
        int rank,
        Long faqId,
        String question,
        InquiryCategoryResponse category,
        InquiryFaqAudienceResponse audience,
        @Schema(description = "기간 안에 문의한 서로 다른 회원 수(N명 문의)")
        long askedMemberCount
    ) {
    }
}
