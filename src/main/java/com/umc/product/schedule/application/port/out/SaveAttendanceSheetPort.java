package com.umc.product.schedule.application.port.out;

import com.umc.product.schedule.domain.AttendanceSheet;

//출석부 저장
public interface SaveAttendanceSheetPort {

    /**
     * 출석부 저장
     *
     * @param sheet 출석부
     * @return 저장된 출석부
     */
    AttendanceSheet save(AttendanceSheet sheet);
}
