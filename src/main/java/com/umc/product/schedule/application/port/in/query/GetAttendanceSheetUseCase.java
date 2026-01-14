package com.umc.product.schedule.application.port.in.query;

import com.umc.product.schedule.application.port.in.query.dto.AttendanceSheetInfo;
import com.umc.product.schedule.domain.AttendanceSheet.AttendanceSheetId;
import java.util.List;

/**
 * 출석부 조회 UseCase
 */
public interface GetAttendanceSheetUseCase {

    /**
     * 출석부 조회
     *
     * @param sheetId 출석부 ID
     * @return 출석부 정보
     */
    AttendanceSheetInfo getById(AttendanceSheetId sheetId);

    /**
     * 일정별 출석부 조회
     *
     * @param scheduleId 일정 ID
     * @return 출석부 정보
     */
    AttendanceSheetInfo getByScheduleId(Long scheduleId);

    /**
     * 일정 목록별 출석부 목록 조회
     *
     * @param scheduleIds 일정 ID 목록
     * @return 출석부 정보 목록
     */
    List<AttendanceSheetInfo> getByScheduleIds(List<Long> scheduleIds);
}
