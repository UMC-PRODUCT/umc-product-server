package com.umc.product.inquiry.adapter.in.web.dto.response;

import com.umc.product.inquiry.application.port.in.query.dto.InquiryFaqAudienceInfo;

public record InquiryFaqAudienceResponse(
    Long audienceId,
    String name
) {
    public static InquiryFaqAudienceResponse from(InquiryFaqAudienceInfo info) {
        return info == null ? null : new InquiryFaqAudienceResponse(info.audienceId(), info.name());
    }
}
