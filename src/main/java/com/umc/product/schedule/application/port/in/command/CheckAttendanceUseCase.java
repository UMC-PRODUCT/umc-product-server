package com.umc.product.schedule.application.port.in.command;

import com.umc.product.schedule.domain.AttendanceRecord.AttendanceRecordId;

/**
 * 출석 체크 UseCase
 */
public interface CheckAttendanceUseCase {

    /**
     * 출석 체크
     *
     * @param command 출석 체크 Command
     * @return 생성된 출석 기록 ID
     */
    AttendanceRecordId check(CheckAttendanceCommand command);
}
