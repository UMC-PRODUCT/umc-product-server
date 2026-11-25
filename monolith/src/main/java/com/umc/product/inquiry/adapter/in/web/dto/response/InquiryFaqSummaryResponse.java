package com.umc.product.inquiry.adapter.in.web.dto.response;

import com.umc.product.inquiry.application.port.in.query.dto.InquiryFaqSummaryInfo;

public record InquiryFaqSummaryResponse(
    Long faqId,
    String question,
    InquiryCategoryResponse category,
    InquiryFaqAudienceResponse audience
) {
    public static InquiryFaqSummaryResponse from(InquiryFaqSummaryInfo info) {
        return info == null ? null : new InquiryFaqSummaryResponse(
            info.faqId(),
            info.question(),
            InquiryCategoryResponse.from(info.category()),
            InquiryFaqAudienceResponse.from(info.audience())
        );
    }
}
