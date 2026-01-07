package com.umc.product.schedule.application.port.out;

import com.umc.product.schedule.domain.ScheduleAttendance;
import com.umc.product.schedule.domain.enums.AttendanceStatus;
import java.util.List;
import java.util.Optional;

public interface LoadScheduleAttendancePort {

    Optional<ScheduleAttendance> findById(Long id);

    List<ScheduleAttendance> findByScheduleId(Long scheduleId);

    List<ScheduleAttendance> findByScheduleIdAndStatusIn(Long scheduleId, List<AttendanceStatus> statuses);

    /**
     * 스케줄별 전체 인원 수
     */
    Integer countByScheduleId(Long scheduleId);

    /**
     * 스케줄별 출석 인원 수 (PRESENT, LATE 포함)
     */
    Integer countPresentByScheduleId(Long scheduleId);

    /**
     * 스케줄별 승인 대기 인원 수
     */
    Integer countPendingByScheduleId(Long scheduleId);

    /**
     * 멤버의 출석 가능한 세션 조회 (PENDING 또는 *_PENDING 상태)
     */
    List<ScheduleAttendance> findAvailableByMemberId(Long memberId);

    /**
     * 멤버의 출석 히스토리 조회 (완료된 것들)
     */
    List<ScheduleAttendance> findHistoryByMemberId(Long memberId);
}
