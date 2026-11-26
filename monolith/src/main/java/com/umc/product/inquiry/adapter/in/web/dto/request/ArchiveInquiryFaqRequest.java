package com.umc.product.inquiry.adapter.in.web.dto.request;

import com.umc.product.inquiry.application.port.in.command.dto.ArchiveInquiryFaqCommand;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ArchiveInquiryFaqRequest(
    @Schema(description = "화면에서 본 대표 질문의 version. 그사이 다른 운영진이 고쳤으면 409 INQUIRY-0029")
    @NotNull @PositiveOrZero Long expectedVersion
) {

    public ArchiveInquiryFaqCommand toCommand(Long actorMemberId, Long faqId) {
        return new ArchiveInquiryFaqCommand(actorMemberId, faqId, expectedVersion);
    }
}
