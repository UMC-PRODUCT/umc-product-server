package com.umc.product.inquiry.adapter.in.web.dto.response;

import java.util.List;

import com.umc.product.inquiry.application.port.in.query.dto.InquiryStaffDetailInfo;

import io.swagger.v3.oas.annotations.media.Schema;

public record InquiryStaffDetailResponse(
    @Schema(description = "질문자용 상세와 같은 필드")
    InquiryDetailResponse inquiry,
    @Schema(description = "미배정이면 null. 담당자가 UMC_PRODUCT 권한을 잃어도 null", nullable = true)
    InquiryStaffMemberResponse assignee,
    List<InquiryLabelResponse> labels,
    @Schema(description = "오래된 것부터. 첫 줄은 문의 접수")
    List<InquiryHistoryResponse> histories,
    @Schema(description = "연결된 대표 질문. null이면 미분류", nullable = true)
    InquiryFaqRefResponse faq,
    @Schema(description = "질문자가 문의 전에 \"먼저 확인해 보세요\"로 본 대표 질문", nullable = true)
    InquiryFaqRefResponse referencedFaq
) {
    public static InquiryStaffDetailResponse from(InquiryStaffDetailInfo info) {
        return new InquiryStaffDetailResponse(
            InquiryDetailResponse.from(info.detail()),
            InquiryStaffMemberResponse.from(info.assignee()),
            info.labels().stream().map(InquiryLabelResponse::from).toList(),
            info.histories().stream().map(InquiryHistoryResponse::from).toList(),
            InquiryFaqRefResponse.from(info.faq()),
            InquiryFaqRefResponse.from(info.referencedFaq())
        );
    }
}
