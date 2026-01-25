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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceSheetQueryService implements GetAttendanceSheetUseCase {

    private final LoadAttendanceSheetPort loadAttendanceSheetPort;

    @Override
    public AttendanceSheetInfo getById(AttendanceSheetId sheetId) {
        AttendanceSheet sheet = loadAttendanceSheetPort.findById(sheetId.id())
                .orElseThrow(
                        () -> new BusinessException(Domain.SCHEDULE, ScheduleErrorCode.ATTENDANCE_SHEET_NOT_FOUND));
        return AttendanceSheetInfo.from(sheet);
    }

    @Override
    public AttendanceSheetInfo getByScheduleId(Long scheduleId) {
        return loadAttendanceSheetPort.findByScheduleId(scheduleId)
                .map(AttendanceSheetInfo::from)
                .orElse(null);
    }

    @Override
    public List<AttendanceSheetInfo> getByScheduleIds(List<Long> scheduleIds) {
        return loadAttendanceSheetPort.findByScheduleIds(scheduleIds).stream()
                .map(AttendanceSheetInfo::from)
                .toList();
    }
}
