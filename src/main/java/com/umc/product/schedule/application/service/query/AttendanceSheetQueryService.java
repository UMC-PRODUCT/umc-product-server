package com.umc.product.schedule.application.service.query;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.schedule.application.port.in.query.GetAttendanceSheetUseCase;
import com.umc.product.schedule.application.port.in.query.dto.AttendanceSheetInfo;
import com.umc.product.schedule.application.port.out.LoadAttendanceSheetPort;
import com.umc.product.schedule.domain.AttendanceSheet;
import com.umc.product.schedule.domain.AttendanceSheet.AttendanceSheetId;
import com.umc.product.schedule.domain.exception.ScheduleErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 출석부(AttendanceSheet) 조회를 담당
 * <p>출석부 ID 또는 일정(Schedule) ID 기준으로 조회
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceSheetQueryService implements GetAttendanceSheetUseCase {

    private final LoadAttendanceSheetPort loadAttendanceSheetPort;

    //출석부 기준 -> 출석부 별도 기능 제거로 삭제 예정
    @Override
    public AttendanceSheetInfo getById(AttendanceSheetId sheetId) {
        AttendanceSheet sheet = loadAttendanceSheetPort.findById(sheetId.id())
            .orElseThrow(
                () -> new BusinessException(Domain.SCHEDULE, ScheduleErrorCode.ATTENDANCE_SHEET_NOT_FOUND));
        return AttendanceSheetInfo.from(sheet);
    }

    //일정기준
    @Override
    public AttendanceSheetInfo getByScheduleId(Long scheduleId) {
        return loadAttendanceSheetPort.findByScheduleId(scheduleId)
            .map(AttendanceSheetInfo::from)
            .orElse(null);
    }

    //목록
    @Override
    public List<AttendanceSheetInfo> getByScheduleIds(List<Long> scheduleIds) {
        return loadAttendanceSheetPort.findByScheduleIds(scheduleIds).stream()
            .map(AttendanceSheetInfo::from)
            .toList();
    }
}
