package com.umc.product.inquiry.adapter.in.web.dto.request;

import com.umc.product.inquiry.application.port.in.command.dto.SaveInquiryLabelCommand;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record SaveInquiryLabelRequest(
    @Schema(description = "라벨 이름. 앞뒤 공백을 빼고 1~30자, 겹치면 409")
    @NotNull String name
) {

    public SaveInquiryLabelCommand toCommand(Long actorMemberId, Long labelId) {
        return new SaveInquiryLabelCommand(actorMemberId, labelId, name);
    }
}
