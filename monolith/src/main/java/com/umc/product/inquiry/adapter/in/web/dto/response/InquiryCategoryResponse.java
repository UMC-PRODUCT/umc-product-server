package com.umc.product.inquiry.adapter.in.web.dto.response;

import com.umc.product.inquiry.application.port.in.query.dto.InquiryCategoryInfo;

public record InquiryCategoryResponse(
    Long categoryId,
    String name
) {
    public static InquiryCategoryResponse from(InquiryCategoryInfo info) {
        return info == null ? null : new InquiryCategoryResponse(info.categoryId(), info.name());
    }
}
