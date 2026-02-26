package com.umc.product.schedule.application.port.out;

import com.umc.product.schedule.domain.AttendanceRecord;
import java.util.List;

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
     * 여러명 출석 기록 한 번에 저장
     *
     * @param records 출석 기록
     */
    void saveAllRecords(List<AttendanceRecord> records);
}
