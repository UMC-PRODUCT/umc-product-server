package com.umc.product.schedule.adapter.in.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
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
    Instant startTime,

    @Schema(description = "종료 시간", example = "12:00:00")
    Instant endTime,

    @Schema(description = "출석부 ID", example = "1")
    Long sheetId,

    @Schema(description = "출석 기록 ID", example = "1")
    Long recordId,

    @Schema(
        description = "출석 상태",
        example = "PENDING",
        allowableValues = {"PENDING", "PRESENT", "PRESENT_PENDING", "LATE", "LATE_PENDING"}
    )
    String status,

    @Schema(description = "출석 상태 표시", example = "출석 전")
    String statusDisplay,

    @Schema(description = "위치 인증 여부 (출석 전이면 null, 출석 후에는 출석 시점의 값)", example = "true")
    Boolean locationVerified
) {
}
