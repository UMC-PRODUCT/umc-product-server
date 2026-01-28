package com.umc.product.schedule.application.port.in.command;

import com.umc.product.schedule.application.port.in.command.dto.CreateScheduleWithAttendanceCommand;

/**
 * 일정 + 출석부 통합 생성 Facade UseCase
 */
public interface CreateScheduleWithAttendanceUseCase {

    /**
     * 일정과 출석부를 함께 생성
     *
     * @param command 일정 + 출석부 생성 Command
     * @return 생성된 일정 ID
     */
    Long create(CreateScheduleWithAttendanceCommand command);
}
