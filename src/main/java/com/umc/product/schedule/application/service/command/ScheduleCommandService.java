package com.umc.product.schedule.application.service.command;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfoWithStatus;
import com.umc.product.schedule.application.port.in.command.CreateScheduleUseCase;
import com.umc.product.schedule.application.port.in.command.DeleteScheduleUseCase;
import com.umc.product.schedule.application.port.in.command.UpdateScheduleUseCase;
import com.umc.product.schedule.application.port.in.command.dto.CreateScheduleCommand;
import com.umc.product.schedule.application.port.in.command.dto.UpdateScheduleCommand;
import com.umc.product.schedule.application.port.out.DeleteAttendanceRecordPort;
import com.umc.product.schedule.application.port.out.DeleteAttendanceSheetPort;
import com.umc.product.schedule.application.port.out.DeleteSchedulePort;
import com.umc.product.schedule.application.port.out.LoadAttendanceSheetPort;
import com.umc.product.schedule.application.port.out.LoadSchedulePort;
import com.umc.product.schedule.application.port.out.SaveSchedulePort;
import com.umc.product.schedule.domain.Schedule;
import com.umc.product.schedule.domain.exception.ScheduleDomainException;
import com.umc.product.schedule.domain.exception.ScheduleErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ScheduleCommandService implements CreateScheduleUseCase, UpdateScheduleUseCase, DeleteScheduleUseCase {

    private final SaveSchedulePort saveSchedulePort;
    private final LoadSchedulePort loadSchedulePort;
    private final DeleteSchedulePort deleteSchedulePort;

    private final LoadAttendanceSheetPort loadAttendanceSheetPort;
    private final DeleteAttendanceSheetPort deleteAttendanceSheetPort;

    private final DeleteAttendanceRecordPort deleteAttendanceRecordPort;

    // 외부 도메인 UseCase
    private final GetChallengerUseCase getChallengerUseCase;


    // 일정 생성
    @Override
    public Long create(CreateScheduleCommand command) {
        // 작성자의 Challenger 상태 조회 (탈부, 제명 상태일 시 exeption)
        ChallengerInfoWithStatus challengerInfoWithStatus = getChallengerUseCase.getLatestActiveChallengerByMemberId(
            command.authorMemberId());
        Long authorChallengerId = challengerInfoWithStatus.challengerId();

        // Schedule 생성 및 저장
        Schedule schedule = command.toEntity(authorChallengerId);
        Schedule savedSchedule = saveSchedulePort.save(schedule);

        return savedSchedule.getId();
    }
    
    // 일정 수정
    @Override
    public void update(UpdateScheduleCommand command) {
        Schedule schedule = loadSchedulePort.findById(command.scheduleId())
            .orElseThrow(() -> new ScheduleDomainException(ScheduleErrorCode.SCHEDULE_NOT_FOUND));

        schedule.update(
            command.name(),
            command.description(),
            command.tags(),
            command.startsAt(),
            command.endsAt(),
            command.isAllDay(),
            command.locationName(),
            command.location()
        );

        saveSchedulePort.save(schedule);
    }

    // 일정 삭제
    @Override
    public void delete(Long scheduleId) {
        if (!loadSchedulePort.existsById(scheduleId)) {
            throw new ScheduleDomainException(ScheduleErrorCode.SCHEDULE_NOT_FOUND);
        }

        // 해당 Schedule의 AttendanceSheet 조회
        loadAttendanceSheetPort.findByScheduleId(scheduleId)
            .ifPresent(sheet -> {
                // Sheet에 연결된 모든 Record 삭제
                deleteAttendanceRecordPort.deleteAllBySheetId(sheet.getId());
            });

        // Sheet 삭제
        deleteAttendanceSheetPort.deleteByScheduleId(scheduleId);

        // Schedule 삭제
        deleteSchedulePort.delete(scheduleId);
    }
}
