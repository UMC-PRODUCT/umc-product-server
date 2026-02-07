package com.umc.product.schedule.adapter.in.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalTime;
import java.util.List;

@Schema(description = "출석 가능 일정 응답")
public record AvailableAttendanceResponse(
    @Schema(description = "일정 ID", example = "1")
    Long scheduleId,

    @Schema(description = "일정명", example = "9기 OT")
    String scheduleName,

    @Schema(description = "태그 목록", example = "[\"STUDY\", \"PROJECT\"]")
    List<String> tags,

    @Schema(description = "시작 시간", example = "10:00:00")
    LocalTime startTime,

    @Schema(description = "종료 시간", example = "12:00:00")
    LocalTime endTime,

    @Schema(description = "출석부 ID", example = "1")
    Long sheetId,

    @Schema(description = "출석 기록 ID", example = "1")
    Long recordId,

    @Schema(description = "출석 상태", example = "PENDING")
    String status,

    @Schema(description = "출석 상태 표시", example = "출석 전")
    String statusDisplay
) {
}
