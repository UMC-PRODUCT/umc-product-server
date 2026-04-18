package com.umc.product.schedule.application.service.command;

import com.umc.product.audit.application.port.in.annotation.Audited;
import com.umc.product.audit.domain.AuditAction;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfoWithStatus;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.global.util.GeometryUtils;
import com.umc.product.schedule.application.port.in.command.DeleteScheduleUseCase;
import com.umc.product.schedule.application.port.in.command.UpdateScheduleUseCase;
import com.umc.product.schedule.application.port.out.DeleteSchedulePort;
import com.umc.product.schedule.application.port.out.LoadSchedulePort;
import com.umc.product.schedule.application.port.out.SaveSchedulePort;
import com.umc.product.schedule.application.port.v2.in.command.CreateScheduleUseCase;
import com.umc.product.schedule.application.port.v2.in.command.dto.CreateScheduleCommand;
import com.umc.product.schedule.application.port.v2.in.command.dto.EditScheduleCommand;
import com.umc.product.schedule.application.port.v2.out.DeleteScheduleParticipantPort;
import com.umc.product.schedule.application.port.v2.out.LoadScheduleParticipantPort;
import com.umc.product.schedule.application.port.v2.out.SaveScheduleParticipantPort;
import com.umc.product.schedule.domain.AttendancePolicy;
import com.umc.product.schedule.domain.Schedule;
import com.umc.product.schedule.domain.ScheduleParticipant;
import com.umc.product.schedule.domain.exception.ScheduleDomainException;
import com.umc.product.schedule.domain.exception.ScheduleErrorCode;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleCommandService implements CreateScheduleUseCase, UpdateScheduleUseCase, DeleteScheduleUseCase {

    private final SaveSchedulePort saveSchedulePort;
    private final LoadSchedulePort loadSchedulePort;
    private final DeleteSchedulePort deleteSchedulePort;

    private final SaveScheduleParticipantPort saveScheduleParticipantPort;
    private final DeleteScheduleParticipantPort deleteScheduleParticipantPort;
    private final LoadScheduleParticipantPort loadScheduleParticipantPort;

    // 외부 도메인 UseCase
    private final GetChallengerUseCase getChallengerUseCase;


    // 일정 생성
    @Audited(
        domain = Domain.SCHEDULE,
        action = AuditAction.CREATE,
        targetType = "Schedule",
        targetId = "#result",
        description = "'일정이 생성되었습니다.'"
    )
    @Override
    public Long create(CreateScheduleCommand command) {

        // 작성자의 가장 최근 기수 Challenger 상태 조회 (탈부, 제명 상태일 시 exeption)
        ChallengerInfoWithStatus challengerInfoWithStatus = getChallengerUseCase.getLatestActiveChallengerByMemberId(
            command.authorMemberId());
        Long authorChallengerId = challengerInfoWithStatus.challengerId();

        // Schedule 생성 및 저장
        Schedule schedule = command.toEntity(authorChallengerId);
        Schedule savedSchedule = saveSchedulePort.save(schedule);

        // ScheduleParticipant 생성 및 저장
        List<ScheduleParticipant> participants = command.participantMemberIds().stream()
            .map(memberId -> ScheduleParticipant.builder()
                .memberId(memberId)
                .schedule(savedSchedule)
                .attendance(null)
                .build())
            .toList();

        saveScheduleParticipantPort.saveAll(participants);

        return savedSchedule.getId();
    }

    // 일정 수정
    @Audited(
        domain = Domain.SCHEDULE,
        action = AuditAction.UPDATE,
        targetType = "Schedule",
        targetId = "#command.scheduleId()",
        description = "'일정이 수정되었습니다.'"
    )
    @Override
    public Long update(EditScheduleCommand command) {

        Schedule schedule = loadSchedulePort.findById(command.scheduleId())
            .orElseThrow(() -> new ScheduleDomainException(ScheduleErrorCode.SCHEDULE_NOT_FOUND));

        if (schedule.getEndsAt().isAfter(Instant.now())) {
            throw new ScheduleDomainException(ScheduleErrorCode.SCHEDULE_ENDED, "종료된 일정은 수정 불가합니다.");
        }

        // 대면/비대면 전환 처리
        handleOnlineOfflineConversion(schedule, command);

        // 일반 필드 업데이트 (전환이 아닌 경우에만 location/policy 처리)
        if (!command.isChangingToOnline() && !command.isChangingToOffline()) {
            schedule.update(
                command.name(),
                command.description(),
                command.tags(),
                command.startsAt(),
                command.endsAt(),
                command.location() != null ? command.location().locationName() : null,
                command.location() != null ? GeometryUtils.createPoint(
                    command.location().latitude(),
                    command.location().longitude()
                ) : null,
                createPolicyFromCommand(command, schedule)
            );
        } else {
            // 전환 시에는 location/policy 제외하고 업데이트
            schedule.update(
                command.name(),
                command.description(),
                command.tags(),
                command.startsAt(),
                command.endsAt(),
                null, null, null
            );
        }

        // 참여자 update
        updateParticipants(schedule, command);

        // 일정 update
        saveSchedulePort.save(schedule);

        return schedule.getId();
    }

    // 대면/비대면 전환 처리
    private void handleOnlineOfflineConversion(Schedule schedule, EditScheduleCommand command) {
        // 비대면 전환 시
        if (command.isChangingToOnline()) {
            schedule.convertToOnline();
        } else if (command.isChangingToOffline()) { // 대면 전환 시
            schedule.convertToOffline(
                GeometryUtils.createPoint(
                    command.location().latitude(),
                    command.location().longitude()
                ),
                command.location().locationName(),
                createPolicyFromCommand(command, schedule)
            );
        }
    }

    // command로부터 policy 생성
    private AttendancePolicy createPolicyFromCommand(EditScheduleCommand command, Schedule schedule) {
        if (command.attendancePolicy() == null) {
            return null;
        }

        // 시간이 변경되었으면 새 값, 아니면 기존 값 사용
        Instant startsAt = command.startsAt() != null ? command.startsAt() : schedule.getStartsAt();
        Instant endsAt = command.endsAt() != null ? command.endsAt() : schedule.getEndsAt();

        return Schedule.createAttendancePolicy(
            command.attendancePolicy().checkInStartAt(),
            command.attendancePolicy().onTimeEndAt(),
            command.attendancePolicy().lateEndAt(),
            startsAt,
            endsAt
        );
    }

    // 참여자 업데이트
    private void updateParticipants(Schedule schedule, EditScheduleCommand command) {
        // 참여자 변경 없으면 스킵
        if (!command.hasParticipantsChange()) {
            return;
        }

        Set<Long> newMemberIds = command.participantMemberIds();

        // 기존 참여자 조회
        List<ScheduleParticipant> existingParticipants = loadScheduleParticipantPort.findAllByScheduleId(
            schedule.getId());
        Set<Long> existingMemberIds = existingParticipants.stream()
            .map(ScheduleParticipant::getMemberId)
            .collect(Collectors.toSet());

        // 삭제되어야 할 MemberId
        Set<Long> toDeleteIds = new HashSet<>(existingMemberIds);
        toDeleteIds.removeAll(newMemberIds);

        // 추가되어야 할 MemberId
        Set<Long> toAddIds = new HashSet<>(newMemberIds);
        toAddIds.removeAll(existingMemberIds);

        // 삭제
        if (!toDeleteIds.isEmpty()) {
            List<ScheduleParticipant> participantsToDelete = existingParticipants.stream()
                .filter(p -> toDeleteIds.contains(p.getMemberId()))
                .toList();

            deleteScheduleParticipantPort.deleteAll(participantsToDelete);
        }

        // 추가
        if (!toAddIds.isEmpty()) {
            List<ScheduleParticipant> participantsToAdd = toAddIds.stream()
                .map(memberId -> ScheduleParticipant.builder()
                    .memberId(memberId)
                    .schedule(schedule)
                    .attendance(null)
                    .build())
                .toList();

            saveScheduleParticipantPort.saveAll(participantsToAdd);
        }
    }

    /**
     * 일정 생성자가 참여자 명단에서 제외되지 않았는지 검증
     */
    private void validateAuthorNotRemoved(Long authorChallengerId, List<Long> participantMemberIds) {
        ChallengerInfo authorInfo = getChallengerUseCase.findByIdOrNull(authorChallengerId);

        // 일정 생성자가 탈퇴/제명당한 회원일 경우 관리자가 일정을 수정할 수 있도록 검증 스킵 + 로그 남김
        if (authorInfo == null) {
            log.warn("[Schedule] 일정 생성자 챌린저 정보를 찾을 수 없음: authorChallengerId={}", authorChallengerId);
            return;
        }

        if (!participantMemberIds.contains(authorInfo.memberId())) {
            throw new ScheduleDomainException(ScheduleErrorCode.CANNOT_REMOVE_SCHEDULE_AUTHOR);
        }
    }

    // 일정 삭제
    @Audited(
        domain = Domain.SCHEDULE,
        action = AuditAction.DELETE,
        targetType = "Schedule",
        targetId = "#scheduleId",
        description = "'일정이 삭제되었습니다.'"
    )
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
