package com.umc.product.schedule.application.port.in.command;

import com.umc.product.schedule.domain.AttendanceSheet.AttendanceSheetId;

/**
 * 출석부 삭제 UseCase
 */
public interface DeleteAttendanceSheetUseCase {

    /**
     * 출석부 삭제
     *
     * @param sheetId 출석부 ID
     */
    void delete(AttendanceSheetId sheetId);
}
