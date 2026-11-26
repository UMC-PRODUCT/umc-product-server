package com.umc.product.inquiry.application.port.in.query.dto;

import com.umc.product.inquiry.domain.InquiryFaqAudience;

public record InquiryFaqAudienceInfo(
    Long audienceId,
    String name
) {
    public static InquiryFaqAudienceInfo from(InquiryFaqAudience audience) {
        return new InquiryFaqAudienceInfo(audience.getId(), audience.getName());
    }
}
