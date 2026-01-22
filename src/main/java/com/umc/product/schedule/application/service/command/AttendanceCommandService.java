package com.umc.product.schedule.application.service.command;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.schedule.application.port.in.command.ApproveAttendanceUseCase;
import com.umc.product.schedule.application.port.in.command.CheckAttendanceUseCase;
import com.umc.product.schedule.application.port.in.command.dto.CheckAttendanceCommand;
import com.umc.product.schedule.application.port.out.LoadAttendanceRecordPort;
import com.umc.product.schedule.application.port.out.LoadAttendanceSheetPort;
import com.umc.product.schedule.application.port.out.SaveAttendanceRecordPort;
import com.umc.product.schedule.domain.AttendanceRecord;
import com.umc.product.schedule.domain.AttendanceRecord.AttendanceRecordId;
import com.umc.product.schedule.domain.AttendanceSheet;
import com.umc.product.schedule.domain.enums.AttendanceStatus;
import com.umc.product.schedule.domain.exception.ScheduleErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AttendanceCommandService implements CheckAttendanceUseCase, ApproveAttendanceUseCase {

    private final LoadAttendanceSheetPort loadAttendanceSheetPort;
    private final LoadAttendanceRecordPort loadAttendanceRecordPort;
    private final SaveAttendanceRecordPort saveAttendanceRecordPort;

    @Override
    public AttendanceRecordId check(CheckAttendanceCommand command) {
        // 출석부 조회 및 검증
        AttendanceSheet sheet = loadAttendanceSheetPort.findById(command.attendanceSheetId())
                .orElseThrow(
                        () -> new BusinessException(Domain.SCHEDULE, ScheduleErrorCode.ATTENDANCE_SHEET_NOT_FOUND));

        // 출석부 활성 상태 검증
        if (!sheet.isActive()) {
            throw new BusinessException(Domain.SCHEDULE, ScheduleErrorCode.ATTENDANCE_SHEET_INACTIVE);
        }

        // 기존 출석 기록 조회
        AttendanceRecord record = loadAttendanceRecordPort
                .findBySheetIdAndMemberId(command.attendanceSheetId(), command.challengerId())
                .orElseThrow(
                        () -> new BusinessException(Domain.SCHEDULE, ScheduleErrorCode.ATTENDANCE_RECORD_NOT_FOUND));

        // 시간에 따른 출석 상태 결정
        AttendanceStatus newStatus = sheet.determineStatusByTime(command.checkedAt());

        // 출석 체크 처리
        record.checkIn(newStatus, command.checkedAt());

        // 저장
        AttendanceRecord savedRecord = saveAttendanceRecordPort.save(record);

        return savedRecord.getAttendanceRecordId();
    }

    @Override
    public void approve(AttendanceRecordId recordId, Long confirmerId) {
        // 출석 기록 조회
        AttendanceRecord record = loadAttendanceRecordPort.findById(recordId.id())
                .orElseThrow(
                        () -> new BusinessException(Domain.SCHEDULE, ScheduleErrorCode.ATTENDANCE_RECORD_NOT_FOUND));

        // 승인 처리
        record.approve();

        // 저장
        saveAttendanceRecordPort.save(record);
    }

    @Override
    public void reject(AttendanceRecordId recordId, Long confirmerId) {
        // 출석 기록 조회
        AttendanceRecord record = loadAttendanceRecordPort.findById(recordId.id())
                .orElseThrow(
                        () -> new BusinessException(Domain.SCHEDULE, ScheduleErrorCode.ATTENDANCE_RECORD_NOT_FOUND));

        // 반려 처리
        record.reject();

        // 저장
        saveAttendanceRecordPort.save(record);
    }
}
