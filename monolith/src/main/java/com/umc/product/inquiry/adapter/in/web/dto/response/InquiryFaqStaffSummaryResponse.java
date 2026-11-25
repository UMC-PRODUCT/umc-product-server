package com.umc.product.inquiry.adapter.in.web.dto.response;

import java.time.Instant;

import com.umc.product.inquiry.application.port.in.query.dto.InquiryFaqStaffSummaryInfo;
import com.umc.product.inquiry.domain.enums.InquiryFaqStatus;

import io.swagger.v3.oas.annotations.media.Schema;

public record InquiryFaqStaffSummaryResponse(
    Long faqId,
    InquiryFaqStatus status,
    @Schema(description = "수정본 질문")
    String question,
    InquiryCategoryResponse category,
    @Schema(description = "발행 중에 고친 내용이 있어 기존 발행본이 공개 중인지(수정 중 · 기존 발행본 공개)")
    boolean hasUnpublishedChanges,
    @Schema(description = "마지막 발행 시각. 발행한 적 없으면 null", nullable = true)
    Instant publishedAt,
    @Schema(description = "\"해결됐어요\"를 누른 회원 수")
    long resolvedCount,
    Instant updatedAt
) {
    public static InquiryFaqStaffSummaryResponse from(InquiryFaqStaffSummaryInfo info) {
        return new InquiryFaqStaffSummaryResponse(
            info.faqId(),
            info.status(),
            info.question(),
            InquiryCategoryResponse.from(info.category()),
            info.hasUnpublishedChanges(),
            info.publishedAt(),
            info.resolvedCount(),
            info.updatedAt()
        );
    }
}
