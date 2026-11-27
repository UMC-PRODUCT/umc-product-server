package com.umc.product.inquiry.adapter.in.web.dto.request;

import java.util.List;
import java.util.Set;

import com.umc.product.inquiry.application.port.in.command.dto.ReplaceInquiryLabelsCommand;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReplaceInquiryLabelsRequest(
    @Schema(description = "문의에 붙일 라벨 전체. 빈 목록이면 모두 뗀다")
    @NotNull List<@NotNull @Positive Long> labelIds
) {

    public ReplaceInquiryLabelsCommand toCommand(Long inquiryId, Long actorMemberId) {
        return new ReplaceInquiryLabelsCommand(inquiryId, actorMemberId, Set.copyOf(labelIds));
    }
}
