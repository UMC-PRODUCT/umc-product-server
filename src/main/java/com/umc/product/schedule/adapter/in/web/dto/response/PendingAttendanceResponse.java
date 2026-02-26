package com.umc.product.schedule.adapter.in.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "승인 대기 출석 응답")
public record PendingAttendanceResponse(
    @Schema(description = "출석 기록 ID", example = "1")
    Long attendanceId,

    @Schema(description = "멤버 ID", example = "1")
    Long memberId,

    @Schema(description = "멤버 이름", example = "왈왈왈")
    String memberName,

    @Schema(description = "닉네임", example = "길동이")
    String nickname,

    @Schema(description = "프로필 이미지", example = "https://example.com/profile.jpg")
    String profileImageLink,

    @Schema(description = "학교명", example = "중앙대학교")
    String schoolName,

    @Schema(description = "출석 상태", example = "PRESENT_PENDING")
    String status,

    @Schema(description = "사유", example = "병원 방문으로 인한 지각")
    String reason,

    @Schema(description = "요청 일시", example = "2026-03-16T10:05:00Z")
    Instant requestedAt
) {
}
