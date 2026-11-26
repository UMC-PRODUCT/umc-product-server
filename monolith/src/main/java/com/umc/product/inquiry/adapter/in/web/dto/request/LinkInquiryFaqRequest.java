package com.umc.product.inquiry.adapter.in.web.dto.request;

import com.umc.product.inquiry.application.port.in.command.dto.LinkInquiryFaqCommand;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;

public record LinkInquiryFaqRequest(
    @Schema(description = "연결할 대표 질문 ID. null이면 연결을 끊는다", nullable = true)
    @Positive Long faqId
) {

    public LinkInquiryFaqCommand toCommand(Long inquiryId, Long actorMemberId) {
        return new LinkInquiryFaqCommand(inquiryId, actorMemberId, faqId);
    }
}
