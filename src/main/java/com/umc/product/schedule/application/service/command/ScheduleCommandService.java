package com.umc.product.schedule.application.service.command;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
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
import com.umc.product.schedule.application.port.out.SaveAttendanceRecordPort;
import com.umc.product.schedule.application.port.out.SaveAttendanceSheetPort;
import com.umc.product.schedule.application.port.out.SaveSchedulePort;
import com.umc.product.schedule.domain.AttendanceRecord;
import com.umc.product.schedule.domain.AttendanceSheet;
import com.umc.product.schedule.domain.Schedule;
import com.umc.product.schedule.domain.enums.AttendanceStatus;
import com.umc.product.schedule.domain.exception.ScheduleErrorCode;
import com.umc.product.schedule.domain.vo.AttendanceWindow;
import java.util.List;
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

    private final SaveAttendanceSheetPort saveAttendanceSheetPort;
    private final LoadAttendanceSheetPort loadAttendanceSheetPort;
    private final DeleteAttendanceSheetPort deleteAttendanceSheetPort;

    private final SaveAttendanceRecordPort saveAttendanceRecordPort;
    private final DeleteAttendanceRecordPort deleteAttendanceRecordPort;

    // 외부 도메인 UseCase
    private final GetChallengerUseCase getChallengerUseCase;
    private final GetGisuUseCase getGisuUseCase;


    // 일정 생성
    @Override
    public Long create(CreateScheduleCommand command) {
        // 1. 현재 활성 기수 조회
        Long currentGisuId = getGisuUseCase.getActiveGisuId();

        // 2. 작성자의 Challenger 조회 (Member + 현재 기수 기반)
        ChallengerInfo challengerInfo = getChallengerUseCase.getByMemberIdAndGisuId(
                command.authorMemberId(),
                currentGisuId
        );

        if (challengerInfo == null) {
            throw new BusinessException(Domain.SCHEDULE, ScheduleErrorCode.NOT_ACTIVE_CHALLENGER);
        }

        Long authorChallengerId = challengerInfo.challengerId();

        // TODO: 3. 참여자 Member 검증 (필요하다고 판단 시 추후 추가, 검색해서 들어가는 거라서 필요 없을 수도)

        // 4. Schedule 생성 및 저장
        Schedule schedule = command.toEntity(authorChallengerId);
        Schedule savedSchedule = saveSchedulePort.save(schedule);

        // 5. 참여자가 있을 시, 출석 관련 엔티티 생성
        if (command.hasParticipants()) {
            createAttendanceForParticipants(savedSchedule, command.participantMemberIds());
        }

        return savedSchedule.getId();
    }

    private void createAttendanceForParticipants(
            Schedule schedule,
            List<Long> participantMemberIds
    ) {
        // AttendanceSheet 생성
        AttendanceSheet sheet = AttendanceSheet.builder()
                .scheduleId(schedule.getId())
                .window(AttendanceWindow.ofDefault(schedule.getStartsAt()))
                .requiresApproval(false)
                .build();

        AttendanceSheet savedSheet = saveAttendanceSheetPort.save(sheet);

        // 각 참여자 Member AttendanceRecord 생성
        List<AttendanceRecord> records = participantMemberIds.stream()
                .map(memberId -> AttendanceRecord.builder()
                        .attendanceSheetId(savedSheet.getId())
                        .memberId(memberId)
                        .status(AttendanceStatus.PENDING)
                        .build())
                .toList();

        saveAttendanceRecordPort.saveAllRecords(records);
    }

    // 일정 수정
    @Override
    public void update(UpdateScheduleCommand command) {
        Schedule schedule = loadSchedulePort.findById(command.scheduleId())
                .orElseThrow(() -> new BusinessException(Domain.SCHEDULE, ScheduleErrorCode.SCHEDULE_NOT_FOUND));

        schedule.update(
                command.name(),
                command.description(),
                command.scheduleType(),
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
            throw new BusinessException(Domain.SCHEDULE, ScheduleErrorCode.SCHEDULE_NOT_FOUND);
        }

        // 1. 해당 Schedule의 AttendanceSheet 조회
        loadAttendanceSheetPort.findByScheduleId(scheduleId)
                .ifPresent(sheet -> {
                    // 2. Sheet에 연결된 모든 Record 삭제
                    deleteAttendanceRecordPort.deleteAllBySheetId(sheet.getId());
                });

        // 3. Sheet 삭제
        deleteAttendanceSheetPort.deleteByScheduleId(scheduleId);

        // 4. Schedule 삭제
        deleteSchedulePort.delete(scheduleId);
    }
}
