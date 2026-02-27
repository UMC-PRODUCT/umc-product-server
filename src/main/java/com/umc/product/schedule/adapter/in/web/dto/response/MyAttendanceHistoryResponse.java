package com.umc.product.schedule.adapter.in.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.time.LocalDate;
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

    @Schema(description = "일정 날짜", example = "2024-01-15")
    LocalDate scheduledDate,

    @Schema(description = "시작 시간", example = "2024-01-15T14:35:00Z")
    Instant startTime,

    @Schema(description = "종료 시간", example = "2024-01-15T15:35:00Z")
    Instant endTime,

    @Schema(
        description = "출석 상태 (확정된 상태만 반환)",
        example = "PRESENT",
        allowableValues = {"PRESENT", "LATE", "ABSENT"}
    )
    String status,

    @Schema(description = "출석 상태 표시", example = "출석")
    String statusDisplay,

    @Schema(description = "출석부 ID", example = "1")
    Long sheetId,

    @Schema(description = "일정 장소", example = "신촌 캠퍼스")
    String locationName,

    @Schema(description = "위치 인증 여부", example = "true")
    Boolean locationVerified,

    @Schema(description = "출석 메모/사유", example = "지각 사유: 지하철 지연")
    String memo,

    @Schema(description = "실제 출석 체크 시간", example = "2024-01-15T17:35:00Z")
    Instant checkedAt
) {
}
