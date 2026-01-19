package com.umc.product.schedule.application.port.out;

public interface DeleteAttendanceSheetPort {
    void deleteByScheduleId(Long scheduleId);
}
