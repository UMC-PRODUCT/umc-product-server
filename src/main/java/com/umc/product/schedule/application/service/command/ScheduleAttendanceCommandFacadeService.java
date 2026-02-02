package com.umc.product.schedule.application.service.command;

import com.umc.product.schedule.application.port.in.command.CreateAttendanceSheetUseCase;
import com.umc.product.schedule.application.port.in.command.CreateScheduleUseCase;
import com.umc.product.schedule.application.port.in.command.CreateScheduleWithAttendanceUseCase;
import com.umc.product.schedule.application.port.in.command.DeleteScheduleUseCase;
import com.umc.product.schedule.application.port.in.command.DeleteScheduleWithAttendanceUseCase;
import com.umc.product.schedule.application.port.in.command.dto.CreateAttendanceSheetCommand;
import com.umc.product.schedule.application.port.in.command.dto.CreateScheduleCommand;
import com.umc.product.schedule.application.port.in.command.dto.CreateScheduleWithAttendanceCommand;
import com.umc.product.schedule.application.port.out.DeleteAttendanceSheetPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Schedule + AttendanceSheet 통합 Command Facade Service
 * <p>
 * 기존 UseCase들을 조합하여 통합 생성/삭제를 제공합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleAttendanceCommandFacadeService implements
    CreateScheduleWithAttendanceUseCase,
    DeleteScheduleWithAttendanceUseCase {

    private final CreateScheduleUseCase createScheduleUseCase;
    private final CreateAttendanceSheetUseCase createAttendanceSheetUseCase;
    private final DeleteScheduleUseCase deleteScheduleUseCase;
    private final DeleteAttendanceSheetPort deleteAttendanceSheetPort;

    @Override
    public Long create(CreateScheduleWithAttendanceCommand command) {
        // 1. 일정 생성
        CreateScheduleCommand scheduleCommand = command.toScheduleCommand();
        Long scheduleId = createScheduleUseCase.create(scheduleCommand);

        // 2. 출석부 생성
        CreateAttendanceSheetCommand sheetCommand = command.toAttendanceSheetCommand(scheduleId);
        createAttendanceSheetUseCase.create(sheetCommand);

        return scheduleId;
    }

    @Override
    public void delete(Long scheduleId) {
        // 1. 출석부 삭제 (있는 경우)
        deleteAttendanceSheetPort.deleteByScheduleId(scheduleId);

        // 2. 일정 삭제
        deleteScheduleUseCase.delete(scheduleId);
    }
}
