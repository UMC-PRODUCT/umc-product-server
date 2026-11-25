package com.umc.product.inquiry.adapter.in.web.dto.response;

import java.time.Instant;

import com.umc.product.inquiry.application.port.in.query.dto.InquiryFaqStaffDetailInfo;
import com.umc.product.inquiry.domain.enums.InquiryFaqStatus;

import io.swagger.v3.oas.annotations.media.Schema;

public record InquiryFaqStaffDetailResponse(
    Long faqId,
    @Schema(description = "대표 질문 버전. 초안 저장·발행·보관 요청의 expectedVersion에 그대로 넣는다. 고칠 때마다 바뀐다")
    long version,
    InquiryFaqStatus status,
    @Schema(description = "발행 중에 고친 내용이 있어 기존 발행본이 공개 중인지")
    boolean hasUnpublishedChanges,
    @Schema(description = "수정본. 초안 저장은 이 내용을 통째로 바꾼다")
    InquiryFaqContentResponse draft,
    @Schema(description = "지금 공개 중이거나 마지막으로 공개했던 발행본. 발행한 적 없으면 null", nullable = true)
    InquiryFaqContentResponse published,
    @Schema(description = "수정일. 마지막 발행 시각", nullable = true)
    Instant publishedAt,
    @Schema(description = "마지막으로 발행한 사람", nullable = true)
    InquiryStaffMemberResponse publishedBy,
    @Schema(description = "검증 조직. 늘 UMC PRODUCT")
    String verifiedBy,
    long resolvedCount
) {
    public static InquiryFaqStaffDetailResponse from(InquiryFaqStaffDetailInfo info) {
        return new InquiryFaqStaffDetailResponse(
            info.faqId(),
            info.version(),
            info.status(),
            info.hasUnpublishedChanges(),
            InquiryFaqContentResponse.from(info.draft()),
            InquiryFaqContentResponse.from(info.published()),
            info.publishedAt(),
            InquiryStaffMemberResponse.from(info.publishedBy()),
            info.verifiedBy(),
            info.resolvedCount()
        );
    }
}
