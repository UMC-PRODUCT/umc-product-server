package com.umc.product.schedule.application.service.query;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.schedule.application.port.in.query.GetPendingAttendancesUseCase;
import com.umc.product.schedule.application.port.in.query.dto.PendingAttendanceInfo;
import com.umc.product.schedule.application.port.out.LoadAttendanceRecordPort;
import com.umc.product.schedule.application.port.out.LoadAttendanceSheetPort;
import com.umc.product.schedule.domain.AttendanceRecord;
import com.umc.product.schedule.domain.AttendanceSheet;
import com.umc.product.schedule.domain.exception.ScheduleErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PendingAttendanceQueryService implements GetPendingAttendancesUseCase {

    private final LoadAttendanceSheetPort loadAttendanceSheetPort;
    private final LoadAttendanceRecordPort loadAttendanceRecordPort;

    //멤버 의존성 고민
    @Override
    public List<PendingAttendanceInfo> getPendingList(Long scheduleId) {
        AttendanceSheet sheet = loadAttendanceSheetPort.findByScheduleId(scheduleId)
                .orElseThrow(
                        () -> new BusinessException(Domain.SCHEDULE, ScheduleErrorCode.ATTENDANCE_SHEET_NOT_FOUND));

        List<AttendanceRecord> pendingRecords = loadAttendanceRecordPort.findPendingRecordsBySheetId(sheet.getId());

        if (pendingRecords.isEmpty()) {
            return List.of();
        }

        return pendingRecords.stream()
                .map(PendingAttendanceInfo::from)
                .toList();
    }
}
