package com.umc.product.inquiry.adapter.in.web.dto.response;

public record CreateInquiryResponse(
    Long inquiryId
) {
    public static CreateInquiryResponse from(Long inquiryId) {
        return new CreateInquiryResponse(inquiryId);
    }
}
