package com.umc.product.schedule.adapter.in.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "출석 기록 응답")
public record AttendanceRecordResponse(
    @Schema(description = "출석 기록 ID", example = "1")
    Long id,

    @Schema(description = "출석부 ID", example = "1")
    Long attendanceSheetId,

    @Schema(description = "멤버 ID", example = "1")
    Long memberId,

    @Schema(description = "출석 상태", example = "PRESENT")
    String status,

    @Schema(description = "메모", example = "병원 방문으로 인한 지각")
    String memo
) {
}
