package com.umc.product.curriculum.adapter.in.web.v2.dto.request;

import com.umc.product.curriculum.application.port.in.command.dto.workbook.ExcuseChallengerWorkbookCommand;

import jakarta.validation.constraints.NotBlank;

public record ExcuseChallengerWorkbookRequest(
    @NotBlank(message = "인정 처리 사유는 필수입니다.") String reason
) {

    public ExcuseChallengerWorkbookCommand toCommand(Long challengerWorkbookId, Long excuseApprovedMemberId) {
        return ExcuseChallengerWorkbookCommand.builder()
            .challengerWorkbookId(challengerWorkbookId)
            .excuseApprovedMemberId(excuseApprovedMemberId)
            .reason(reason)
            .build();
    }
}
