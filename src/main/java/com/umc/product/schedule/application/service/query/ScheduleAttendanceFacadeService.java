package com.umc.product.schedule.application.service.query;

import com.umc.product.schedule.application.port.in.query.GetAttendanceSheetUseCase;
import com.umc.product.schedule.application.port.in.query.GetScheduleDetailUseCase;
import com.umc.product.schedule.application.port.in.query.GetScheduleWithAttendanceUseCase;
import com.umc.product.schedule.application.port.in.query.dto.AttendanceSheetInfo;
import com.umc.product.schedule.application.port.in.query.dto.ScheduleDetailInfo;
import com.umc.product.schedule.application.port.in.query.dto.ScheduleWithAttendanceInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Schedule + AttendanceSheet 통합 조회 Facade Service
 * <p>
 * 기존 UseCase들을 조합하여 통합 응답을 제공합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleAttendanceFacadeService implements GetScheduleWithAttendanceUseCase {

    private final GetScheduleDetailUseCase getScheduleDetailUseCase;
    private final GetAttendanceSheetUseCase getAttendanceSheetUseCase;

    @Override
    public ScheduleWithAttendanceInfo getScheduleWithAttendance(Long scheduleId) {
        // 일정 상세 조회
        ScheduleDetailInfo scheduleInfo = getScheduleDetailUseCase.getScheduleDetail(scheduleId);

        // 출석부 조회 (없으면 null)
        AttendanceSheetInfo attendanceInfo = getAttendanceSheetUseCase.getByScheduleId(scheduleId);

        return ScheduleWithAttendanceInfo.of(scheduleInfo, attendanceInfo);
    }
}
