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
import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.schedule.application.port.out.DeleteAttendanceRecordPort;
import com.umc.product.schedule.application.port.out.DeleteAttendanceSheetPort;
import com.umc.product.schedule.application.port.out.DeleteSchedulePort;
import com.umc.product.schedule.application.port.out.LoadAttendanceRecordPort;
import com.umc.product.schedule.application.port.out.LoadAttendanceSheetPort;
import com.umc.product.schedule.application.port.out.LoadSchedulePort;
import com.umc.product.schedule.application.port.out.SaveAttendanceRecordPort;
import com.umc.product.schedule.application.port.out.SaveSchedulePort;
import com.umc.product.schedule.domain.AttendanceRecord;
import com.umc.product.schedule.domain.AttendanceSheet;
import com.umc.product.schedule.domain.Schedule;
import com.umc.product.schedule.domain.enums.AttendanceStatus;
import com.umc.product.schedule.domain.exception.ScheduleDomainException;
import com.umc.product.schedule.domain.exception.ScheduleErrorCode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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

    private final LoadAttendanceRecordPort loadAttendanceRecordPort;
    private final SaveAttendanceRecordPort saveAttendanceRecordPort;
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

        // 출석부가 있으면
        if (attendanceSheetOpt.isPresent()) {
            AttendanceSheet sheet = attendanceSheetOpt.get();

            // 1. 시간대 동기화: 일정 시간이 변경되었으면 출석부 시간대 이동
            if (command.startsAt() != null) {
                Duration diff = Duration.between(oldStartsAt, command.startsAt());
                if (!diff.isZero()) {
                    sheet.shiftWindow(diff);
                }
            }

            // 2. 참여자 명단 동기화: participantMemberIds가 제공되었으면
            if (command.participantMemberIds() != null) {
                syncAttendanceRecords(sheet, command.participantMemberIds());
            }
        }

        saveSchedulePort.save(schedule);
    }

    // 일정 위치 수정
    @Override
    public UpdateScheduleLocationInfo updateLocation(UpdateScheduleLocationCommand command) {
        Schedule schedule = loadSchedulePort.findById(command.scheduleId())
            .orElseThrow(() -> new ScheduleDomainException(ScheduleErrorCode.SCHEDULE_NOT_FOUND));

        // 위치 정보 업데이트
        schedule.update(
            null,  // name
            null,  // description
            null,  // tags
            null,  // startsAt
            null,  // endsAt
            null,  // isAllDay
            command.locationName(),
            command.location()
        );

        saveSchedulePort.save(schedule);

        return UpdateScheduleLocationInfo.of(
            schedule.getId(),
            schedule.getLocationName(),
            schedule.getLocation()
        );
    }

    /**
     * 출석부 참여자 명단 동기화
     * <p>
     * 제한 없음: 언제든 자유롭게 참여자 추가/삭제 가능
     * <p>
     * 동작:
     * - 추가된 참여자: AttendanceRecord 생성 (PENDING 상태)
     * - 삭제된 참여자: AttendanceRecord 삭제 (Hard Delete)
     */
    private void syncAttendanceRecords(AttendanceSheet sheet, List<Long> newParticipantIds) {
        Long sheetId = sheet.getId();

        // 기존 출석 기록 조회
        List<AttendanceRecord> existingRecords = loadAttendanceRecordPort.findByAttendanceSheetId(sheetId);

        // 기존 참여자 ID 목록
        Set<Long> existingMemberIds = existingRecords.stream()
            .map(AttendanceRecord::getMemberId)
            .collect(Collectors.toSet());

        Set<Long> newMemberIds = new HashSet<>(newParticipantIds);

        // 추가된 참여자: 새 기록 생성
        Set<Long> toAdd = new HashSet<>(newMemberIds);
        toAdd.removeAll(existingMemberIds);

        if (!toAdd.isEmpty()) {
            List<AttendanceRecord> newRecords = toAdd.stream()
                .map(memberId -> AttendanceRecord.builder()
                    .attendanceSheetId(sheetId)
                    .memberId(memberId)
                    .status(AttendanceStatus.PENDING)
                    .build())
                .toList();
            saveAttendanceRecordPort.saveAllRecords(newRecords);
        }

        // 삭제된 참여자: 기록 삭제 (Hard Delete)
        Set<Long> toRemove = new HashSet<>(existingMemberIds);
        toRemove.removeAll(newMemberIds);

        if (!toRemove.isEmpty()) {
            List<AttendanceRecord> toDelete = existingRecords.stream()
                .filter(r -> toRemove.contains(r.getMemberId()))
                .toList();

            for (AttendanceRecord record : toDelete) {
                deleteAttendanceRecordPort.delete(record);
            }
        }
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
