package com.umc.product.schedule.application.service.query;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.schedule.application.port.in.query.GetPendingAttendancesUseCase;
import com.umc.product.schedule.application.port.in.query.dto.PendingAttendanceInfo;
import com.umc.product.schedule.application.port.out.LoadAttendanceRecordPort;
import com.umc.product.schedule.application.port.out.LoadAttendanceSheetPort;
import com.umc.product.schedule.domain.AttendanceSheet;
import com.umc.product.schedule.domain.exception.ScheduleErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 관리자용 - 승인 대기 중인(PENDING) 출석 기록 목록을 조회
 * <p>일정 ID로 출석부를 찾고, 해당 출석부의 PENDING 상태 기록을 멤버 정보와 함께 반환.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PendingAttendanceQueryService implements GetPendingAttendancesUseCase {

    private final LoadAttendanceSheetPort loadAttendanceSheetPort;
    private final LoadAttendanceRecordPort loadAttendanceRecordPort;

    @Override
    public List<PendingAttendanceInfo> getPendingList(Long scheduleId) {
        AttendanceSheet sheet = loadAttendanceSheetPort.findByScheduleId(scheduleId)
            .orElseThrow(
                () -> new BusinessException(Domain.SCHEDULE, ScheduleErrorCode.ATTENDANCE_SHEET_NOT_FOUND));

        return loadAttendanceRecordPort.findPendingWithMemberInfo(sheet.getId());
    }
}
