package com.umc.product.schedule.application.port.out;

import com.umc.product.schedule.domain.AttendanceSheet;

@Deprecated(since = "v1.5.0", forRemoval = true)
public interface DeleteAttendanceSheetPort {
    void deleteByScheduleId(Long scheduleId);

    /**
     * 출석부 삭제
     *
     * @param sheet 출석부
     */
    void delete(AttendanceSheet sheet);
}
