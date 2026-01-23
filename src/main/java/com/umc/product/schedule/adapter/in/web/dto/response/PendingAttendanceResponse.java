package com.umc.product.schedule.adapter.in.web.dto.response;

import java.time.LocalDateTime;

public record PendingAttendanceResponse(
        Long attendanceId,
        Long memberId,
        String status,
        String reason,
        LocalDateTime requestedAt
) {
}
