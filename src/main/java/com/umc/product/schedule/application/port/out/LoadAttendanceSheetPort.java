package com.umc.product.schedule.application.port.out;

import com.umc.product.schedule.domain.AttendanceSheet;
import java.util.List;
import java.util.Optional;

//출석부 조회파트
public interface LoadAttendanceSheetPort {

    /**
     * ID로 출석부 조회
     *
     * @param id 출석부 ID
     * @return 출석부
     */
    Optional<AttendanceSheet> findById(Long id);

    /**
     * 일정 ID로 출석부 조회
     *
     * @param scheduleId 일정 ID
     * @return 출석부
     */
    Optional<AttendanceSheet> findByScheduleId(Long scheduleId);

    /**
     * 일정 ID 목록으로 출석부 목록 조회
     *
     * @param scheduleIds 일정 ID 목록
     * @return 출석부 목록
     */
    List<AttendanceSheet> findByScheduleIds(List<Long> scheduleIds);

    /**
     * 출석부 존재 여부 확인
     *
     * @param scheduleId 일정 ID
     * @return 존재 여부
     */
    boolean existsByScheduleId(Long scheduleId);
}
