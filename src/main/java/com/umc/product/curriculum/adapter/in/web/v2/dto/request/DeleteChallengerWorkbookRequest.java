package com.umc.product.curriculum.adapter.in.web.v2.dto.request;

import com.umc.product.curriculum.application.port.in.command.dto.workbook.DeleteChallengerWorkbookCommand;

import jakarta.validation.constraints.NotBlank;

public record DeleteChallengerWorkbookRequest(
    @NotBlank(message = "삭제 사유는 필수입니다.") String reason
) {

    public DeleteChallengerWorkbookCommand toCommand(Long challengerWorkbookId, Long requestedMemberId) {
        return DeleteChallengerWorkbookCommand.builder()
            .challengerWorkbookId(challengerWorkbookId)
            .requestedMemberId(requestedMemberId)
            .reason(reason)
            .build();
    }
}
