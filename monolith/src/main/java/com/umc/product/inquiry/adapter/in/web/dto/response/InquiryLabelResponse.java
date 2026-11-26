package com.umc.product.inquiry.adapter.in.web.dto.response;

import com.umc.product.inquiry.application.port.in.query.dto.InquiryLabelInfo;

public record InquiryLabelResponse(
    Long labelId,
    String name
) {
    public static InquiryLabelResponse from(InquiryLabelInfo info) {
        return new InquiryLabelResponse(info.labelId(), info.name());
    }
}
