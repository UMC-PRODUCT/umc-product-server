package com.umc.product.schedule.adapter.in.web.dto.response;

import com.umc.product.schedule.application.port.in.query.dto.PendingAttendanceInfo;
import java.time.LocalDateTime;

public record PendingAttendanceResponse(
        Long attendanceId,
        Long challengerId,
        String memberName,
        String nickname,
        String schoolName,
        String status,
        String reason,
        LocalDateTime requestedAt
) {
    public static PendingAttendanceResponse from(PendingAttendanceInfo info) {
        return new PendingAttendanceResponse(
                info.attendanceId(),
                info.challengerId(),
                info.memberName(),
                info.nickname(),
                info.schoolName(),
                info.status().name(),
                info.reason(),
                info.requestedAt()
        );
    }
}
