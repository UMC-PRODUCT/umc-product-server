package com.umc.product.schedule.application.service;

import com.umc.product.schedule.application.port.in.query.GetAttendanceRecordUseCase;
import com.umc.product.schedule.application.port.in.query.GetAttendanceSheetUseCase;
import com.umc.product.schedule.application.port.in.query.GetAvailableAttendancesUseCase;
import com.umc.product.schedule.application.port.in.query.GetMyAttendanceHistoryUseCase;
import com.umc.product.schedule.application.port.in.query.GetPendingAttendancesUseCase;
import com.umc.product.schedule.application.port.in.query.dto.AttendanceRecordInfo;
import com.umc.product.schedule.application.port.in.query.dto.AttendanceSheetInfo;
import com.umc.product.schedule.application.port.in.query.dto.AvailableAttendanceInfo;
import com.umc.product.schedule.application.port.in.query.dto.MyAttendanceHistoryInfo;
import com.umc.product.schedule.application.port.in.query.dto.PendingAttendanceInfo;
import com.umc.product.schedule.domain.AttendanceRecord.AttendanceRecordId;
import com.umc.product.schedule.domain.AttendanceSheet.AttendanceSheetId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceQueryService implements
        GetAttendanceRecordUseCase,
        GetAttendanceSheetUseCase,
        GetAvailableAttendancesUseCase,
        GetMyAttendanceHistoryUseCase,
        GetPendingAttendancesUseCase {

    // === GetAttendanceRecordUseCase ===

    @Override
    public AttendanceRecordInfo getById(AttendanceRecordId recordId) {
        // TODO: 구현 필요
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<AttendanceRecordInfo> getBySheetId(Long sheetId) {
        // TODO: 구현 필요
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<AttendanceRecordInfo> getByChallengerId(Long challengerId) {
        // TODO: 구현 필요
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<AttendanceRecordInfo> getPendingBySheetId(Long sheetId) {
        // TODO: 구현 필요
        throw new UnsupportedOperationException("Not implemented yet");
    }

    // === GetAttendanceSheetUseCase ===

    @Override
    public AttendanceSheetInfo getById(AttendanceSheetId sheetId) {
        // TODO: 구현 필요
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public AttendanceSheetInfo getByScheduleId(Long scheduleId) {
        // TODO: 구현 필요
        return null;
    }

    @Override
    public List<AttendanceSheetInfo> getByScheduleIds(List<Long> scheduleIds) {
        // TODO: 구현 필요
        return java.util.Collections.emptyList();
    }

    // === GetAvailableAttendancesUseCase ===

    @Override
    public List<AvailableAttendanceInfo> getAvailableList(Long memberId) {
        // TODO: 구현 필요
        return java.util.Collections.emptyList();
    }

    // === GetMyAttendanceHistoryUseCase ===

    @Override
    public List<MyAttendanceHistoryInfo> getHistory(Long memberId) {
        // TODO: 구현 필요
        return java.util.Collections.emptyList();
    }

    // === GetPendingAttendancesUseCase ===

    @Override
    public List<PendingAttendanceInfo> getPendingList(Long scheduleId) {
        // TODO: 구현 필요
        return java.util.Collections.emptyList();
    }
}
