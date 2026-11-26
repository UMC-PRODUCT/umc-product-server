package com.umc.product.inquiry.adapter.in.web.dto.response;

import com.umc.product.inquiry.application.port.in.query.dto.InquiryFaqRefInfo;

public record InquiryFaqRefResponse(
    Long faqId,
    String question
) {
    public static InquiryFaqRefResponse from(InquiryFaqRefInfo info) {
        return info == null ? null : new InquiryFaqRefResponse(info.faqId(), info.question());
    }
}
