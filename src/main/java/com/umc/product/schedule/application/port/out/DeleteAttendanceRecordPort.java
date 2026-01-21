package com.umc.product.schedule.application.port.out;

import com.umc.product.schedule.domain.AttendanceRecord;

public interface DeleteAttendanceRecordPort {
    void deleteAllBySheetId(Long sheetId);

    /**
     * 출석 기록 삭제
     *
     * @param record 출석 기록
     */
    void delete(AttendanceRecord record);
}
