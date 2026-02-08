package com.umc.product.schedule.application.service.command;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfoWithStatus;
import com.umc.product.schedule.application.port.in.command.CreateScheduleUseCase;
import com.umc.product.schedule.application.port.in.command.DeleteScheduleUseCase;
import com.umc.product.schedule.application.port.in.command.UpdateScheduleUseCase;
import com.umc.product.schedule.application.port.in.command.dto.CreateScheduleCommand;
import com.umc.product.schedule.application.port.in.command.dto.UpdateScheduleCommand;
import com.umc.product.schedule.application.port.in.command.dto.UpdateScheduleLocationCommand;
import com.umc.product.schedule.application.port.in.command.dto.UpdateScheduleLocationInfo;
import com.umc.product.schedule.application.port.out.DeleteAttendanceRecordPort;
import com.umc.product.schedule.application.port.out.DeleteAttendanceSheetPort;
import com.umc.product.schedule.application.port.out.DeleteSchedulePort;
import com.umc.product.schedule.application.port.out.LoadAttendanceSheetPort;
import com.umc.product.schedule.application.port.out.LoadSchedulePort;
import com.umc.product.schedule.application.port.out.SaveSchedulePort;
import com.umc.product.schedule.domain.AttendanceSheet;
import com.umc.product.schedule.domain.Schedule;
import com.umc.product.schedule.domain.exception.ScheduleDomainException;
import com.umc.product.schedule.domain.exception.ScheduleErrorCode;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
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

        // 출석부가 있으면 조회 (없어도 일정 수정 가능)
        var attendanceSheetOpt = loadAttendanceSheetPort.findByScheduleId(command.scheduleId());

        // 변경 전 기존 일정 시작 시간
        LocalDateTime oldStartsAt = schedule.getStartsAt();

        // 일정 정보 업데이트
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

        // 출석부가 있고 일정 시간이 변경되었으면, 그 차이만큼 출석부 시간대 이동
        if (command.startsAt() != null && attendanceSheetOpt.isPresent()) {
            Duration diff = Duration.between(oldStartsAt, command.startsAt());

            if (!diff.isZero()) {
                attendanceSheetOpt.get().shiftWindow(diff);
            }
        }

        saveSchedulePort.save(schedule);
    }

    // 일정 출석체크 위치 수정
    @Override
    public UpdateScheduleLocationInfo updateLocation(UpdateScheduleLocationCommand command) {
        Schedule schedule = loadSchedulePort.findById(command.scheduleId())
            .orElseThrow(() -> new ScheduleDomainException(ScheduleErrorCode.SCHEDULE_NOT_FOUND));

        schedule.updateLocation(command.locationName(), command.location());

        saveSchedulePort.save(schedule);

        return UpdateScheduleLocationInfo.of(schedule.getId(), schedule.getLocationName(), schedule.getLocation());
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
