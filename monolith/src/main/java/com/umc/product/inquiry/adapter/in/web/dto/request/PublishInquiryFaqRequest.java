package com.umc.product.inquiry.adapter.in.web.dto.request;

import com.umc.product.inquiry.application.port.in.command.dto.PublishInquiryFaqCommand;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record PublishInquiryFaqRequest(
    @Schema(description = "화면에서 본 대표 질문의 version. 그사이 다른 운영진이 고쳤으면 409 INQUIRY-0029")
    @NotNull @PositiveOrZero Long expectedVersion,
    @Schema(description = "개인정보가 없는지 확인했다")
    @NotNull Boolean noPersonalInformation,
    @Schema(description = "적용 범위를 확인했다")
    @NotNull Boolean scopeConfirmed,
    @Schema(description = "답변이 정확한지 확인했다")
    @NotNull Boolean answerVerified
) {

    public PublishInquiryFaqCommand toCommand(Long actorMemberId, Long faqId) {
        return new PublishInquiryFaqCommand(
            actorMemberId, faqId, expectedVersion, noPersonalInformation, scopeConfirmed, answerVerified
        );
    }
}
