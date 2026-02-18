package com.umc.product.schedule.adapter.in.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "일정별 승인 대기 출석 목록 응답")
public record PendingAttendancesByScheduleResponse(
    @Schema(description = "일정 ID", example = "30")
    Long scheduleId,

    @Schema(description = "일정명", example = "1주차 세션")
    String scheduleName,

    @Schema(description = "승인 대기 출석 목록")
    List<PendingAttendanceResponse> pendingAttendances
) {
}
