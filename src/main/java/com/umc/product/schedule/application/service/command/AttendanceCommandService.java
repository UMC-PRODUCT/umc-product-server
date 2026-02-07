package com.umc.product.schedule.application.service.command;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.schedule.application.port.in.command.ApproveAttendanceUseCase;
import com.umc.product.schedule.application.port.in.command.CheckAttendanceUseCase;
import com.umc.product.schedule.application.port.in.command.SubmitReasonUseCase;
import com.umc.product.schedule.application.port.in.command.dto.CheckAttendanceCommand;
import com.umc.product.schedule.application.port.in.command.dto.SubmitReasonCommand;
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

/**
 * 출석 체크 및 승인/반려 처리를 오케스트레이션하는 커맨드 서비스.
 * <p>
 * 주요 흐름: 1. check: 출석부 조회 → 활성 상태 보고 → 기존 출석 기록 조회 → AttendanceSheet가 시간 기반으로 상태 판정 → AttendanceRecord에 체크인 처리 2.
 * approve: PENDING 상태의 출석 기록을 승인 → 확정 상태(PRESENT/LATE/EXCUSED)로 3. reject: PENDING 상태의 출석 기록을 거부 → ABSENT로
 * <p>
 * 비즈니스 규칙 판정(시간 판정, 상태 전이)은 도메인 객체(AttendanceSheet, AttendanceRecord)에있음 , 이 서비스는 조회/검증/저장 흐름만
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AttendanceCommandService implements CheckAttendanceUseCase, ApproveAttendanceUseCase, SubmitReasonUseCase {

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
            .findBySheetIdAndMemberId(command.attendanceSheetId(), command.memberId())
            .orElseThrow(
                () -> new BusinessException(Domain.SCHEDULE, ScheduleErrorCode.ATTENDANCE_RECORD_NOT_FOUND));

        // 시간에 따른 출석 상태 결정
        AttendanceStatus newStatus = sheet.determineStatusByTime(command.checkedAt());

        // 출석 체크 처리 (프론트에서 판단한 위치 인증 결과 저장)
        record.checkIn(
            newStatus,
            command.checkedAt(),
            command.latitude(),
            command.longitude(),
            command.locationVerified()
        );

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
        record.approve(confirmerId);

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
        record.reject(confirmerId);

        // 저장

        saveAttendanceRecordPort.save(record);
    }

    @Override
    public AttendanceRecordId submitReason(SubmitReasonCommand command) {
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
            .findBySheetIdAndMemberId(command.attendanceSheetId(), command.memberId())
            .orElseThrow(
                () -> new BusinessException(Domain.SCHEDULE, ScheduleErrorCode.ATTENDANCE_RECORD_NOT_FOUND));

        // 사유 제출 (출석 체크 전 상태에서만 가능, 위치 인증은 실패로 처리됨)
        record.submitReasonBeforeCheck(
            command.reason(),
            java.time.LocalDateTime.ofInstant(command.submittedAt(), java.time.ZoneId.systemDefault())
        );

        // 저장
        AttendanceRecord savedRecord = saveAttendanceRecordPort.save(record);

        return savedRecord.getAttendanceRecordId();
    }
}
