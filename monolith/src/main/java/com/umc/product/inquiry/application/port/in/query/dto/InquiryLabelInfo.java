package com.umc.product.inquiry.application.port.in.query.dto;

import com.umc.product.inquiry.domain.InquiryLabel;

public record InquiryLabelInfo(
    Long labelId,
    String name
) {
    public static InquiryLabelInfo from(InquiryLabel label) {
        return new InquiryLabelInfo(label.getId(), label.getName());
    }
}
