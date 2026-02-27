package com.umc.product.curriculum.adapter.in.web.dto.request;

import com.umc.product.curriculum.application.port.in.command.SelectBestWorkbookCommand;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "베스트 워크북 선정 요청")
public record SelectBestWorkbookRequest(
        @Schema(description = "베스트 선정 이유 (선택)", example = "꼼꼼한 분석과 창의적인 접근이 돋보입니다.")
        String bestReason
) {
    public SelectBestWorkbookCommand toCommand(Long challengerWorkbookId) {
        return new SelectBestWorkbookCommand(challengerWorkbookId, bestReason);
    }
}
