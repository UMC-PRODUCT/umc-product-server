package com.umc.product.schedule.application.service.query;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.schedule.application.port.in.query.GetAttendanceRecordUseCase;
import com.umc.product.schedule.application.port.in.query.dto.AttendanceRecordInfo;
import com.umc.product.schedule.application.port.out.LoadAttendanceRecordPort;
import com.umc.product.schedule.domain.AttendanceRecord;
import com.umc.product.schedule.domain.AttendanceRecord.AttendanceRecordId;
import com.umc.product.schedule.domain.exception.ScheduleErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 출석 기록(AttendanceRecord) 단건/목록 조회
 * <p> 출석부(sheetId) 기준, 챌린저(challengerId) 기준, PENDING 상태 기준으로 조회 가능 상태
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceRecordQueryService implements GetAttendanceRecordUseCase {

    private final LoadAttendanceRecordPort loadAttendanceRecordPort;

    @Override
    public AttendanceRecordInfo getById(AttendanceRecordId recordId) {
        AttendanceRecord record = loadAttendanceRecordPort.findById(recordId.id())
            .orElseThrow(
                () -> new BusinessException(Domain.SCHEDULE, ScheduleErrorCode.ATTENDANCE_RECORD_NOT_FOUND));
        return AttendanceRecordInfo.from(record);
    }

    @Override
    public List<AttendanceRecordInfo> getBySheetId(Long sheetId) {
        return loadAttendanceRecordPort.findByAttendanceSheetId(sheetId).stream()
            .map(AttendanceRecordInfo::from)
            .toList();
    }

    @Override
    public List<AttendanceRecordInfo> getByChallengerId(Long challengerId) {
        return loadAttendanceRecordPort.findByMemberId(challengerId).stream()
            .map(AttendanceRecordInfo::from)
            .toList();
    }

    @Override
    public List<AttendanceRecordInfo> getPendingBySheetId(Long sheetId) {
        return loadAttendanceRecordPort.findPendingRecordsBySheetId(sheetId).stream()
            .map(AttendanceRecordInfo::from)
            .toList();
    }
}
