package com.umc.product.schedule.adapter.in.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "출석부 응답")
public record AttendanceSheetResponse(
    @Schema(description = "출석부 ID", example = "1")
    Long id,

    @Schema(description = "일정 ID", example = "1")
    Long scheduleId,

    @Schema(description = "출석 시작 시간", example = "2026-03-16T10:00:00")
    LocalDateTime startTime,

    @Schema(description = "출석 종료 시간", example = "2026-03-16T12:00:00")
    LocalDateTime endTime,

    @Schema(description = "지각 기준 시간(분)", example = "10")
    int lateThresholdMinutes,

    @Schema(description = "승인 필요 여부", example = "true")
    boolean requiresApproval,

    @Schema(description = "활성화 여부", example = "true")
    boolean active
) {
}
