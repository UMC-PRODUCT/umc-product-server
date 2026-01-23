package com.umc.product.schedule.application.port.in.query.dto;

import com.umc.product.schedule.domain.AttendanceRecord;
import com.umc.product.schedule.domain.enums.AttendanceStatus;
import java.time.LocalDateTime;

public record PendingAttendanceInfo(
        Long attendanceId,
        Long memberId,
        AttendanceStatus status,
        String reason,
        LocalDateTime requestedAt
) {
    public static PendingAttendanceInfo from(AttendanceRecord record) {
        return new PendingAttendanceInfo(
                record.getId(),
                record.getMemberId(),
                record.getStatus(),
                record.getMemo(),
                record.getCheckedAt()
        );
    }
}
