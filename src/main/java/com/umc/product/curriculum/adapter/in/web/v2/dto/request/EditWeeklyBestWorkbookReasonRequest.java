package com.umc.product.curriculum.adapter.in.web.v2.dto.request;

import com.umc.product.curriculum.application.port.in.command.dto.workbook.EditWeeklyBestWorkbookCommand;

import jakarta.validation.constraints.NotBlank;

public record EditWeeklyBestWorkbookReasonRequest(
    @NotBlank(message = "선정 사유는 필수입니다.") String reason
) {

    public EditWeeklyBestWorkbookCommand toCommand(Long weeklyBestWorkbookId, Long requestedMemberId) {
        return EditWeeklyBestWorkbookCommand.builder()
            .weeklyBestWorkbookId(weeklyBestWorkbookId)
            .requestedMemberId(requestedMemberId)
            .newReason(reason)
            .build();
    }
}
