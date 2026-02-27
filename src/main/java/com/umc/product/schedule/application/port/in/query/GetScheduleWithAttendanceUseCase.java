package com.umc.product.schedule.application.port.in.query;

import com.umc.product.schedule.application.port.in.query.dto.ScheduleWithAttendanceInfo;

/**
 * 일정 + 출석부 통합 조회 Facade UseCase
 */
public interface GetScheduleWithAttendanceUseCase {

    /**
     * 일정 상세 조회 (출석부 정보 포함)
     *
     * @param scheduleId 일정 ID
     * @return Schedule + AttendanceSheet 통합 정보
     */
    ScheduleWithAttendanceInfo getScheduleWithAttendance(Long scheduleId);
}
