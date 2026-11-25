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
    List<InquiryHistoryResponse> histories
) {
    public static InquiryStaffDetailResponse from(InquiryStaffDetailInfo info) {
        return new InquiryStaffDetailResponse(
            InquiryDetailResponse.from(info.detail()),
            InquiryStaffMemberResponse.from(info.assignee()),
            info.labels().stream().map(InquiryLabelResponse::from).toList(),
            info.histories().stream().map(InquiryHistoryResponse::from).toList()
        );
    }
}
