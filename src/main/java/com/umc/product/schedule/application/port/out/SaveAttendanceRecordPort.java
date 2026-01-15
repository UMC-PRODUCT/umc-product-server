package com.umc.product.schedule.application.port.out;

import com.umc.product.schedule.domain.AttendanceRecord;

//출석 저장
public interface SaveAttendanceRecordPort {

    /**
     * 출석 기록 저장
     *
     * @param record 출석 기록
     * @return 저장된 출석 기록
     */
    AttendanceRecord save(AttendanceRecord record);

    /**
     * 출석 기록 삭제
     *
     * @param record 출석 기록
     */
    void delete(AttendanceRecord record);
}
