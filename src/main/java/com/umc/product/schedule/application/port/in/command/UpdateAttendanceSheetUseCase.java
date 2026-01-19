package com.umc.product.schedule.application.port.in.command;

import com.umc.product.schedule.domain.AttendanceSheet.AttendanceSheetId;

/**
 * 출석부 수정 UseCase
 */
public interface UpdateAttendanceSheetUseCase {

    /**
     * 출석부 수정
     *
     * @param command 출석부 수정 Command
     */
    void update(UpdateAttendanceSheetCommand command);

    /**
     * 출석부 비활성화
     *
     * @param sheetId 출석부 ID
     */
    void deactivate(AttendanceSheetId sheetId);

    /**
     * 출석부 활성화
     *
     * @param sheetId 출석부 ID
     */
    void activate(AttendanceSheetId sheetId);
}
