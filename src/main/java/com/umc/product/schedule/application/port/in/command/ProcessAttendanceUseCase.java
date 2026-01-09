package com.umc.product.schedule.application.port.in.command;

import com.umc.product.schedule.application.port.in.command.dto.ProcessAttendanceCommand;

public interface ProcessAttendanceUseCase {

    /**
     * 출석 승인
     */
    void approve(ProcessAttendanceCommand command);

    /**
     * 출석 거절
     */
    void reject(ProcessAttendanceCommand command);
}
