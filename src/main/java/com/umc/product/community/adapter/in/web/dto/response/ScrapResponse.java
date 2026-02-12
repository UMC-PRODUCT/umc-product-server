package com.umc.product.community.adapter.in.web.dto.response;

import com.umc.product.community.application.port.in.post.ToggleScrapUseCase;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "스크랩 토글 응답")
public record ScrapResponse(
        @Schema(description = "스크랩 상태 (true: 스크랩, false: 스크랩 취소)", example = "true")
        boolean scrapped,

        @Schema(description = "현재 스크랩 수", example = "15")
        int scrapCount
) {
    public static ScrapResponse from(ToggleScrapUseCase.ScrapResult result) {
        return new ScrapResponse(result.scrapped(), result.scrapCount());
    }
}
