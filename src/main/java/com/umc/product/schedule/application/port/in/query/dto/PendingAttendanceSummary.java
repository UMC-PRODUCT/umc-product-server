package com.umc.product.schedule.application.port.in.query.dto;

import com.umc.product.schedule.domain.enums.AttendanceStatus;
import java.time.LocalDateTime;

public record PendingAttendanceSummary(
        Long attendanceId,
        Long memberId,
        String memberName,
        String nickname,
        String schoolName,
        AttendanceStatus status,
        String reason,
        LocalDateTime requestedAt
) {
    // TODO: 머지 후 from 메서드 추가하기
}
