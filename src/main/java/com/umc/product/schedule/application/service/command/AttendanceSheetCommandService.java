package com.umc.product.schedule.application.service.command;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.schedule.application.port.in.command.CreateAttendanceSheetUseCase;
import com.umc.product.schedule.application.port.in.command.UpdateAttendanceSheetUseCase;
import com.umc.product.schedule.application.port.in.command.dto.CreateAttendanceSheetCommand;
import com.umc.product.schedule.application.port.in.command.dto.UpdateAttendanceSheetCommand;
import com.umc.product.schedule.application.port.out.LoadAttendanceSheetPort;
import com.umc.product.schedule.application.port.out.LoadSchedulePort;
import com.umc.product.schedule.application.port.out.SaveAttendanceSheetPort;
import com.umc.product.schedule.domain.AttendanceSheet;
import com.umc.product.schedule.domain.AttendanceSheet.AttendanceSheetId;
import com.umc.product.schedule.domain.Schedule;
import com.umc.product.schedule.domain.exception.ScheduleErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 출석부(AttendanceSheet) 생성/수정/활성화/비활성화를 처리
 * <p>
 * 출석부는 Schedule(일정)에 1:1로 연결, 출석 가능 시간대(window)와 승인 정책(requiresApproval)을 설정가능
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AttendanceSheetCommandService implements CreateAttendanceSheetUseCase, UpdateAttendanceSheetUseCase {

    private final LoadSchedulePort loadSchedulePort;
    private final LoadAttendanceSheetPort loadAttendanceSheetPort;
    private final SaveAttendanceSheetPort saveAttendanceSheetPort;

    @Override
    public AttendanceSheetId create(CreateAttendanceSheetCommand command) {
        // 일정 존재 확인
        Schedule schedule = loadSchedulePort.findById(command.scheduleId())
            .orElseThrow(() -> new BusinessException(Domain.SCHEDULE, ScheduleErrorCode.SCHEDULE_NOT_FOUND));

        // 출석부 생성
        AttendanceSheet sheet = command.toEntity(schedule);

        // 저장
        AttendanceSheet savedSheet = saveAttendanceSheetPort.save(sheet);

        return savedSheet.getAttendanceSheetId();
    }

    @Override
    public void update(UpdateAttendanceSheetCommand command) {
        // 출석부 조회
        AttendanceSheet sheet = loadAttendanceSheetPort.findById(command.sheetId().id())
            .orElseThrow(
                () -> new BusinessException(Domain.SCHEDULE, ScheduleErrorCode.ATTENDANCE_SHEET_NOT_FOUND));

        // 출석 시간대 업데이트
        sheet.update(command.window());

        // 승인 모드 업데이트
        sheet.updateApprovalMode(command.requiresApproval());

        // 저장 - 변경 감지 의존 탈피 떄매 별도 저장
        saveAttendanceSheetPort.save(sheet);
    }

    @Override
    public void deactivate(AttendanceSheetId sheetId) {
        // 출석부 조회
        AttendanceSheet sheet = loadAttendanceSheetPort.findById(sheetId.id())
            .orElseThrow(
                () -> new BusinessException(Domain.SCHEDULE, ScheduleErrorCode.ATTENDANCE_SHEET_NOT_FOUND));

        // 비활성화 (도메인 로직)
        sheet.deactivate();

        // 저장
        saveAttendanceSheetPort.save(sheet);
    }

    @Override
    public void activate(AttendanceSheetId sheetId) {
        // 출석부 조회
        AttendanceSheet sheet = loadAttendanceSheetPort.findById(sheetId.id())
            .orElseThrow(
                () -> new BusinessException(Domain.SCHEDULE, ScheduleErrorCode.ATTENDANCE_SHEET_NOT_FOUND));

        // 활성화 (도메인 로직)
        sheet.activate();

        // 저장
        saveAttendanceSheetPort.save(sheet);
    }
}
