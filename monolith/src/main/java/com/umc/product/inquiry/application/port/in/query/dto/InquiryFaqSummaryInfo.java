package com.umc.product.inquiry.application.port.in.query.dto;

public record InquiryFaqSummaryInfo(
    Long faqId,
    String question,
    InquiryCategoryInfo category,
    InquiryFaqAudienceInfo audience
) {
}
