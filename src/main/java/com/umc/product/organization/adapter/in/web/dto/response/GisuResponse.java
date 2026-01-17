package com.umc.product.organization.adapter.in.web.dto.response;

import com.umc.product.organization.application.port.in.query.dto.GisuInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "기수 정보")
public record GisuResponse(
        @Schema(description = "기수 ID", example = "1")
        Long gisuId,
        @Schema(description = "기수 번호", example = "8")
        Long generation,
        @Schema(description = "시작일", example = "2024-03-01")
        LocalDate startAt,
        @Schema(description = "종료일", example = "2024-08-31")
        LocalDate endAt,
        @Schema(description = "활성 여부", example = "true")
        boolean isActive
) {
    public static GisuResponse from(GisuInfo info) {
        return new GisuResponse(info.gisuId(), info.generation(), info.startAt(), info.endAt(), info.isActive());
    }
}
