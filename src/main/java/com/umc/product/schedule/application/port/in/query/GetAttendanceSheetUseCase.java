package com.umc.product.schedule.application.port.in.query;

import com.umc.product.schedule.application.port.in.query.dto.AttendanceSheetInfo;
import com.umc.product.schedule.domain.AttendanceSheet.AttendanceSheetId;
import java.util.List;

/**
 * 출석부(AttendanceSheet) 조회 UseCase.
 * <p>
 * 출석부 ID 또는 연결된 일정(Schedule) ID로 조회할 수 있다.
 * <p>
 * 일정과 출석부는 1:1 관계라서  scheduleId 기준 조회는 단건을 반환한다.
 */
public interface GetAttendanceSheetUseCase {

    AttendanceSheetInfo getById(AttendanceSheetId sheetId);

    /**
     * 일정에 연결된 출석부 조회. 출석부가 없으면 null 반환. 일정만 생성되는 경우로 만들어두었는데 현재 상의 결과 해당 기능 보류로 인해 주석처리 예쩡
     */
    AttendanceSheetInfo getByScheduleId(Long scheduleId);

    /**
     * 여러 일정의 출석부를 일괄 조회.
     */
    List<AttendanceSheetInfo> getByScheduleIds(List<Long> scheduleIds);
}
