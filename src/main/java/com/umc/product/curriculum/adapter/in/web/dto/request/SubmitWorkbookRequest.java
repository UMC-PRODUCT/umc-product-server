package com.umc.product.curriculum.adapter.in.web.dto.request;

import com.umc.product.curriculum.application.port.in.command.SubmitWorkbookCommand;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "워크북 제출 요청")
public record SubmitWorkbookRequest(
        @Schema(description = "제출 내용 (링크 또는 메모, PLAIN 타입인 경우 생략 가능)", example = "https://github.com/user/repo")
        String submission
) {
    public SubmitWorkbookCommand toCommand(Long challengerWorkbookId) {
        return new SubmitWorkbookCommand(challengerWorkbookId, submission);
    }
}
