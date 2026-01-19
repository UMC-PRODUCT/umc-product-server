package com.umc.product.schedule.application.port.out;

public interface DeleteAttendanceRecordPort {
    void deleteAllBySheetId(Long sheetId);
}
