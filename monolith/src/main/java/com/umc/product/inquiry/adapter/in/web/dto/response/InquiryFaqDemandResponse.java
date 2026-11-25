package com.umc.product.inquiry.adapter.in.web.dto.response;

import com.umc.product.inquiry.application.port.in.query.dto.InquiryFaqDemandInfo;

import io.swagger.v3.oas.annotations.media.Schema;

public record InquiryFaqDemandResponse(
    Long faqId,
    String question,
    @Schema(description = "최근 24시간 동안 문의한 서로 다른 회원 수")
    long askedMemberCount
) {
    public static InquiryFaqDemandResponse from(InquiryFaqDemandInfo info) {
        return new InquiryFaqDemandResponse(info.faqId(), info.question(), info.askedMemberCount());
    }
}
