package com.umc.product.schedule.adapter.in.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "내 출석 이력 응답")
public record MyAttendanceHistoryResponse(
    @Schema(description = "출석 기록 ID", example = "1")
    Long attendanceId,

    @Schema(description = "일정 ID", example = "1")
    Long scheduleId,

    @Schema(description = "일정명", example = "9기 OT")
    String scheduleName,

    @Schema(description = "일정 태그", example = "[\"SEMINAR\", \"ALL\"]")
    List<String> tag,

    @Schema(description = "시간 표시", example = "14:30")
    String time,

    @Schema(description = "출석 상태", example = "PRESENT")
    String status,

    @Schema(description = "출석 상태 표시", example = "출석")
    String statusDisplay
) {
}
