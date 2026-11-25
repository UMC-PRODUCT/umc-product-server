package com.umc.product.inquiry.adapter.in.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record InquiryFaqSuggestionResponse(
    @Schema(description = "보여 줄 대표 질문이 없으면 null", nullable = true)
    InquiryFaqSummaryResponse faq
) {
}
