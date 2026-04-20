package com.umc.product.schedule.adapter.in.web.v1.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "출석부 수정 요청")
public record UpdateAttendanceSheetRequest(
    @Schema(description = "출석 시작 시간 (UTC ISO8601. 예: 2026-03-16T01:00:00Z)", example = "2026-03-16T01:00:00Z")
    Instant startTime,

    @Schema(description = "출석 종료 시간 (UTC ISO8601. 예: 2026-03-16T03:00:00Z)", example = "2026-03-16T03:00:00Z")
    Instant endTime,

    @Schema(description = "지각 기준 시간 (분)", example = "10")
    Integer lateThresholdMinutes,

    @Schema(description = "승인 필요 여부", example = "true")
    boolean requiresApproval
) {
}
