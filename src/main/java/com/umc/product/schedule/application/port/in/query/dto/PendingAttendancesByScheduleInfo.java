package com.umc.product.schedule.application.port.in.query.dto;

import java.util.List;

/**
 * 일정별 승인 대기 출석 목록 정보
 * <p>
 * scheduleId별로 그룹핑된 승인 대기 출석 정보를 담는 DTO. 전체 승인 대기 조회 시 사용.
 */
public record PendingAttendancesByScheduleInfo(
    Long scheduleId,
    String scheduleName,
    List<PendingAttendanceInfo> pendingAttendances
) {
    public static PendingAttendancesByScheduleInfo of(
        Long scheduleId,
        String scheduleName,
        List<PendingAttendanceInfo> pendingAttendances
    ) {
        return new PendingAttendancesByScheduleInfo(
            scheduleId,
            scheduleName,
            pendingAttendances
        );
    }
}
