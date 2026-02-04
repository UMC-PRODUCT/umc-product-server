package com.umc.product.schedule.application.service.command;

import com.umc.product.schedule.application.port.in.command.CreateScheduleUseCase;
import com.umc.product.schedule.application.port.in.command.CreateScheduleWithAttendanceUseCase;
import com.umc.product.schedule.application.port.in.command.DeleteScheduleUseCase;
import com.umc.product.schedule.application.port.in.command.DeleteScheduleWithAttendanceUseCase;
import com.umc.product.schedule.application.port.in.command.dto.CreateScheduleCommand;
import com.umc.product.schedule.application.port.in.command.dto.CreateScheduleWithAttendanceCommand;
import com.umc.product.schedule.application.port.out.DeleteAttendanceSheetPort;
import com.umc.product.schedule.application.port.out.SaveAttendanceRecordPort;
import com.umc.product.schedule.application.port.out.SaveAttendanceSheetPort;
import com.umc.product.schedule.domain.AttendanceRecord;
import com.umc.product.schedule.domain.AttendanceSheet;
import com.umc.product.schedule.domain.enums.AttendanceStatus;
import java.util.List;
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
    private final DeleteScheduleUseCase deleteScheduleUseCase;
    private final DeleteAttendanceSheetPort deleteAttendanceSheetPort;
    private final SaveAttendanceSheetPort saveAttendanceSheetPort;
    private final SaveAttendanceRecordPort saveAttendanceRecordPort;

    @Override
    public Long create(CreateScheduleWithAttendanceCommand command) {
        // 1. 일정 생성
        CreateScheduleCommand scheduleCommand = command.toScheduleCommand();
        Long scheduleId = createScheduleUseCase.create(scheduleCommand);

        // 2. 출석부 생성 - 일정 시간 기준으로 AttendanceSheet가 Window를 조립 (지각 기준 10분 고정)
        AttendanceSheet sheet = AttendanceSheet.createWithSchedule(
            scheduleId,
            command.gisuId(),
            command.startsAt(),
            command.endsAt(),
            command.requiresApproval()
        );
        AttendanceSheet savedSheet = saveAttendanceSheetPort.save(sheet);

        // 3. 참여자별 출석 기록(AttendanceRecord) 생성
        List<Long> participantMemberIds = command.participantMemberIds();
        if (participantMemberIds != null && !participantMemberIds.isEmpty()) {
            List<AttendanceRecord> records = participantMemberIds.stream()
                .distinct()
                .map(memberId -> AttendanceRecord.builder()
                    .attendanceSheetId(savedSheet.getId())
                    .memberId(memberId)
                    .status(AttendanceStatus.PENDING)
                    .build())
                .toList();

            saveAttendanceRecordPort.saveAllRecords(records);
        }

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
