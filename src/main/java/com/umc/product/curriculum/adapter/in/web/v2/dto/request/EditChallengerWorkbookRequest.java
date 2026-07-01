package com.umc.product.curriculum.adapter.in.web.v2.dto.request;

import com.umc.product.curriculum.application.port.in.command.dto.workbook.EditChallengerWorkbookCommand;

import jakarta.validation.constraints.NotBlank;

public record EditChallengerWorkbookRequest(
    @NotBlank(message = "수정할 워크북 내용은 필수입니다.") String content
) {

    public EditChallengerWorkbookCommand toCommand(Long challengerWorkbookId, Long requestedMemberId) {
        return EditChallengerWorkbookCommand.builder()
            .challengerWorkbookId(challengerWorkbookId)
            .requestedMemberId(requestedMemberId)
            .content(content)
            .build();
    }
}
