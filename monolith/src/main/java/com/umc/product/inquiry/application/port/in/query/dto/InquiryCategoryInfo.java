package com.umc.product.inquiry.application.port.in.query.dto;

import com.umc.product.inquiry.domain.InquiryCategory;

public record InquiryCategoryInfo(
    Long categoryId,
    String name
) {
    public static InquiryCategoryInfo from(InquiryCategory category) {
        return new InquiryCategoryInfo(category.getId(), category.getName());
    }
}
