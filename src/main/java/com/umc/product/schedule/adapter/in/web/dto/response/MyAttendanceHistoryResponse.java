package com.umc.product.schedule.adapter.in.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내 출석 이력 응답")
public record MyAttendanceHistoryResponse(
    @Schema(description = "출석 기록 ID", example = "1")
    Long attendanceId,

    @Schema(description = "일정 ID", example = "1")
    Long scheduleId,

    @Schema(description = "일정명", example = "9기 OT")
    String scheduleName,

    @Schema(description = "주차 표시", example = "1주차")
    String weekDisplay,

    @Schema(description = "날짜 표시", example = "03.16 (토)")
    String dateDisplay,

    @Schema(description = "출석 상태", example = "PRESENT")
    String status,

    @Schema(description = "출석 상태 표시", example = "출석")
    String statusDisplay
) {
}
