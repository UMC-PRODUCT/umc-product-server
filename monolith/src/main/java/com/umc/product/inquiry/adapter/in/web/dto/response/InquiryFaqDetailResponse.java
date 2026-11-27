package com.umc.product.inquiry.adapter.in.web.dto.response;

import java.time.Instant;

import com.umc.product.inquiry.application.port.in.query.dto.InquiryFaqDetailInfo;

import io.swagger.v3.oas.annotations.media.Schema;

public record InquiryFaqDetailResponse(
    Long faqId,
    String question,
    String answer,
    InquiryCategoryResponse category,
    InquiryFaqAudienceResponse audience,
    @Schema(description = "검증 조직. 늘 UMC PRODUCT")
    String verifiedBy,
    @Schema(description = "수정일. 마지막 발행 시각")
    Instant updatedAt,
    @Schema(description = "관련 안내. 없거나 공개 중이 아니면 null", nullable = true)
    InquiryFaqRefResponse related
) {
    public static InquiryFaqDetailResponse from(InquiryFaqDetailInfo info) {
        return new InquiryFaqDetailResponse(
            info.faqId(),
            info.question(),
            info.answer(),
            InquiryCategoryResponse.from(info.category()),
            InquiryFaqAudienceResponse.from(info.audience()),
            info.verifiedBy(),
            info.updatedAt(),
            InquiryFaqRefResponse.from(info.related())
        );
    }
}
