package com.umc.product.schedule.application.service.command;

import com.umc.product.audit.application.port.in.annotation.Audited;
import com.umc.product.audit.domain.AuditAction;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.schedule.application.port.in.command.ApproveAttendanceUseCase;
import com.umc.product.schedule.application.port.in.command.CheckAttendanceUseCase;
import com.umc.product.schedule.application.port.in.command.SubmitReasonUseCase;
import com.umc.product.schedule.application.port.in.command.dto.CheckAttendanceCommand;
import com.umc.product.schedule.application.port.in.command.dto.SubmitReasonCommand;
import com.umc.product.schedule.application.port.out.LoadAttendanceRecordPort;
import com.umc.product.schedule.application.port.out.LoadAttendanceSheetPort;
import com.umc.product.schedule.application.port.out.LoadSchedulePort;
import com.umc.product.schedule.application.port.out.SaveAttendanceRecordPort;
import com.umc.product.schedule.domain.AttendanceRecord;
import com.umc.product.schedule.domain.AttendanceRecord.AttendanceRecordId;
import com.umc.product.schedule.domain.AttendanceSheet;
import com.umc.product.schedule.domain.Schedule;
import com.umc.product.schedule.domain.enums.AttendanceStatus;
import com.umc.product.schedule.domain.exception.ScheduleDomainException;
import com.umc.product.schedule.domain.exception.ScheduleErrorCode;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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
@Deprecated(since = "v1.5.0", forRemoval = true)
public class AttendanceCommandService implements CheckAttendanceUseCase, ApproveAttendanceUseCase, SubmitReasonUseCase {

    private final LoadAttendanceSheetPort loadAttendanceSheetPort;
    private final LoadAttendanceRecordPort loadAttendanceRecordPort;
    private final SaveAttendanceRecordPort saveAttendanceRecordPort;
    private final LoadSchedulePort loadSchedulePort;

    private final ApplicationEventPublisher eventPublisher;

    @Audited(
        domain = Domain.SCHEDULE,
        action = AuditAction.CHECK,
        targetType = "AttendanceRecord",
        targetId = "#result",
        description = "'출석 체크'"
    )
    @Override
    public Long check(CheckAttendanceCommand command) {
        // 출석부 조회 및 검증
        AttendanceSheet sheet = loadAttendanceSheetPort.findById(command.attendanceSheetId())
            .orElseThrow(
                () -> new ScheduleDomainException(ScheduleErrorCode.ATTENDANCE_SHEET_NOT_FOUND));

        // 출석부 활성화 여부 확인
        if (!sheet.isActive()) {
            throw new ScheduleDomainException(ScheduleErrorCode.ATTENDANCE_SHEET_INACTIVE);
        }

        // 일정 조회 (지각/결석 판별용)
        Schedule schedule = loadSchedulePort.findById(sheet.getScheduleId())
            .orElseThrow(
                () -> new ScheduleDomainException(ScheduleErrorCode.SCHEDULE_NOT_FOUND));

        Instant checkTime = command.checkedAt();
        Instant windowStartTime = sheet.getWindow().getStartTime();

        // 출석 가능 시간 범위 검증
        // 0. 출석 시작 전 - 상태 변경 없이 막기
        if (checkTime.isBefore(windowStartTime)) {
            throw new ScheduleDomainException(ScheduleErrorCode.OUTSIDE_ATTENDANCE_WINDOW);
        }

        // 1. 출석 인정 시간 (윈도우 내) - 출석 또는 지각
        if (sheet.isWithinTimeWindow(checkTime)) {
            // OK
        }
        // 2. 윈도우 종료 ~ 일정 종료: 일반 일정은 지각, 종일 일정은 출석
        else if (checkTime.isAfter(sheet.getWindow().getEndTime())
            && !checkTime.isAfter(schedule.getEndsAt())) {
            // OK
        }
        // 3. 일정 종료 후 - 결석 (determineAttendanceStatus에서 처리)

        // 기존 출석 기록 조회
        AttendanceRecord record = loadAttendanceRecordPort
            .findBySheetIdAndMemberId(command.attendanceSheetId(), command.memberId())
            .orElseThrow(
                () -> new ScheduleDomainException(ScheduleErrorCode.ATTENDANCE_RECORD_NOT_FOUND));

        // 시간에 따른 출석 상태 결정
        AttendanceStatus newStatus = determineAttendanceStatus(sheet, schedule, checkTime);

        // 출석 체크 처리 (프론트에서 판단한 위치 인증 결과 저장)
        record.checkIn(
            newStatus,
            checkTime,
            command.latitude(),
            command.longitude(),
            command.locationVerified()
        );

        // 저장
        AttendanceRecord savedRecord = saveAttendanceRecordPort.save(record);

        return savedRecord.getAttendanceRecordId().id();
    }

    private AttendanceStatus determineAttendanceStatus(AttendanceSheet sheet, Schedule schedule, Instant checkTime) {
        // 종일 일정은 지각 개념 없이 일정 종료 전까지 모두 출석 처리
        if (schedule.isAllDay()) {
            if (!checkTime.isAfter(schedule.getEndsAt())) {
                return sheet.isRequiresApproval()
                    ? AttendanceStatus.PRESENT_PENDING
                    : AttendanceStatus.PRESENT;
            }
            return AttendanceStatus.ABSENT;
        }

        // 출석 인정 시간
        if (sheet.isWithinTimeWindow(checkTime)) {
            return sheet.determineStatusByTime(checkTime);
        }

        // 지각 시간
        if (checkTime.isAfter(sheet.getWindow().getEndTime())
            && !checkTime.isAfter(schedule.getEndsAt())) {
            return sheet.isRequiresApproval()
                ? AttendanceStatus.LATE_PENDING
                : AttendanceStatus.LATE;
        }

        // 결석
        return AttendanceStatus.ABSENT;
    }

    @Audited(
        domain = Domain.SCHEDULE,
        action = AuditAction.APPROVE,
        targetType = "AttendanceRecord",
        targetId = "#recordId.id()",
        description = "'출석 승인'"
    )
    @Override
    public void approve(AttendanceRecordId recordId, Long confirmerId) {
        // 출석 기록 조회
        AttendanceRecord record = loadAttendanceRecordPort.findById(recordId.id())
            .orElseThrow(
                () -> new ScheduleDomainException(ScheduleErrorCode.ATTENDANCE_RECORD_NOT_FOUND));

        // 승인 처리
        record.approve(confirmerId);

        // 저장
        saveAttendanceRecordPort.save(record);
    }

    @Audited(
        domain = Domain.SCHEDULE,
        action = AuditAction.REJECT,
        targetType = "AttendanceRecord",
        targetId = "#recordId.id()",
        description = "'출석 반려'"
    )
    @Override
    public void reject(AttendanceRecordId recordId, Long confirmerId) {
        // 출석 기록 조회
        AttendanceRecord record = loadAttendanceRecordPort.findById(recordId.id())
            .orElseThrow(
                () -> new ScheduleDomainException(ScheduleErrorCode.ATTENDANCE_RECORD_NOT_FOUND));

        // 반려 처리
        record.reject(confirmerId);

        // 저장

        saveAttendanceRecordPort.save(record);
    }

    @Audited(
        domain = Domain.SCHEDULE,
        action = AuditAction.SUBMIT,
        targetType = "AttendanceRecord",
        targetId = "#result.id()",
        description = "'출석 사유 제출'"
    )
    @Override
    public AttendanceRecordId submitReason(SubmitReasonCommand command) {
        // 출석부 조회 및 검증
        AttendanceSheet sheet = loadAttendanceSheetPort.findById(command.attendanceSheetId())
            .orElseThrow(
                () -> new ScheduleDomainException(ScheduleErrorCode.ATTENDANCE_SHEET_NOT_FOUND));

        // 출석 시간대 검증 (active 대신 시간대 체크)
        Instant submittedAt = command.submittedAt();
        if (!sheet.isWithinTimeWindow(submittedAt)) {
            throw new ScheduleDomainException(ScheduleErrorCode.OUTSIDE_ATTENDANCE_WINDOW);
        }

        // 기존 출석 기록 조회
        AttendanceRecord record = loadAttendanceRecordPort
            .findBySheetIdAndMemberId(command.attendanceSheetId(), command.memberId())
            .orElseThrow(
                () -> new ScheduleDomainException(ScheduleErrorCode.ATTENDANCE_RECORD_NOT_FOUND));

        // 사유 제출 (출석 체크 전 상태에서만 가능, 위치 인증은 실패로 처리됨)
        record.submitReasonBeforeCheck(command.reason(), submittedAt);

        // 저장
        AttendanceRecord savedRecord = saveAttendanceRecordPort.save(record);

        return savedRecord.getAttendanceRecordId();
    }
}
