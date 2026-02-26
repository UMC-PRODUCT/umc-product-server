package com.umc.product.schedule.application.port.in.command;

/**
 * 일정 + 출석부 통합 삭제 Facade UseCase
 */
public interface DeleteScheduleWithAttendanceUseCase {

    /**
     * 일정과 연결된 출석부를 함께 삭제
     *
     * @param scheduleId 일정 ID
     */
    void delete(Long scheduleId);
}
