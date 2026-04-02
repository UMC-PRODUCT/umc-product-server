package com.umc.product.curriculum.adapter.in.web.v1.dto.request;

import com.umc.product.curriculum.application.port.in.command.dto.UpdateBestReasonCommand;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "베스트 추천사 수정 요청")
public record UpdateBestReasonRequest(
    @Schema(description = "수정할 베스트 추천사", example = "꼼꼼한 분석과 창의적인 접근이 돋보입니다.")
    String bestReason
) {
    public UpdateBestReasonCommand toCommand(Long reviewId, Long memberId) {
        return new UpdateBestReasonCommand(reviewId, memberId, bestReason);
    }
}
