package com.umc.product.schedule.application.port.in.command;

import com.umc.product.schedule.application.port.in.command.dto.CreateAttendanceSheetCommand;
import com.umc.product.schedule.domain.AttendanceSheet.AttendanceSheetId;

/**
 * 출석부 생성 UseCase
 */
public interface CreateAttendanceSheetUseCase {

    /**
     * 출석부 생성
     *
     * @param command 출석부 생성 Command
     * @return 생성된 출석부 ID
     */
    AttendanceSheetId create(CreateAttendanceSheetCommand command);
}
