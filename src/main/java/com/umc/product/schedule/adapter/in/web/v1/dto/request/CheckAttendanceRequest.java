package com.umc.product.schedule.adapter.in.web.v1.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "출석 체크 요청")
public record CheckAttendanceRequest(
    @Schema(description = "출석부 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long attendanceSheetId,

    @Schema(description = "위도", example = "37.5665")
    Double latitude,

    @Schema(description = "경도", example = "126.9780")
    Double longitude,

    @Schema(description = "위치 인증 성공 여부 (프론트에서 판단)", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    Boolean locationVerified
) {
}
