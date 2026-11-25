package com.umc.product.inquiry.adapter.in.web.dto.request;

import com.umc.product.inquiry.application.port.in.command.dto.PublishInquiryFaqCommand;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record PublishInquiryFaqRequest(
    @Schema(description = "개인정보가 없는지 확인했다")
    @NotNull Boolean noPersonalInformation,
    @Schema(description = "적용 범위를 확인했다")
    @NotNull Boolean scopeConfirmed,
    @Schema(description = "답변이 정확한지 확인했다")
    @NotNull Boolean answerVerified
) {

    public PublishInquiryFaqCommand toCommand(Long actorMemberId, Long faqId) {
        return new PublishInquiryFaqCommand(actorMemberId, faqId, noPersonalInformation, scopeConfirmed, answerVerified);
    }
}
