package com.umc.product.schedule.application.service.command;

import com.umc.product.audit.application.port.in.annotation.Audited;
import com.umc.product.audit.domain.AuditAction;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfoWithStatus;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.global.util.GeometryUtils;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.organization.application.port.in.query.dto.GisuInfo;
import com.umc.product.schedule.application.port.in.command.CreateScheduleUseCase;
import com.umc.product.schedule.application.port.in.command.UpdateScheduleUseCase;
import com.umc.product.schedule.application.port.in.command.dto.CreateScheduleCommand;
import com.umc.product.schedule.application.port.in.command.dto.EditScheduleCommand;
import com.umc.product.schedule.application.port.out.DeleteScheduleParticipantPort;
import com.umc.product.schedule.application.port.out.LoadScheduleParticipantPort;
import com.umc.product.schedule.application.port.out.LoadSchedulePort;
import com.umc.product.schedule.application.port.out.SaveScheduleParticipantPort;
import com.umc.product.schedule.application.port.out.SaveSchedulePort;
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
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleCommandService implements CreateScheduleUseCase, UpdateScheduleUseCase {

    private final SaveSchedulePort saveSchedulePort;
    private final LoadSchedulePort loadSchedulePort;

    private final SaveScheduleParticipantPort saveScheduleParticipantPort;
    private final DeleteScheduleParticipantPort deleteScheduleParticipantPort;
    private final LoadScheduleParticipantPort loadScheduleParticipantPort;

    // 외부 도메인 UseCase
    private final GetChallengerUseCase getChallengerUseCase;
    private final GetGisuUseCase getGisuUseCase;


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

        // 생성하려는 일정의 날짜가 현재 기수 활동 기간에서 벗어난 경우 에러 반환
        GisuInfo activeGisu = getGisuUseCase.getActiveGisu();
        if (command.startsAt().isBefore(activeGisu.startAt()) || command.endsAt().isAfter(activeGisu.endAt())) {
            throw new ScheduleDomainException(ScheduleErrorCode.NOT_ACTIVE_GISU_SCHEDULE);
        }

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

        command.validate();

        Schedule schedule = loadSchedulePort.findById(command.scheduleId())
            .orElseThrow(() -> new ScheduleDomainException(ScheduleErrorCode.SCHEDULE_NOT_FOUND));

        if (schedule.getEndsAt().isBefore(Instant.now())) {
            throw new ScheduleDomainException(ScheduleErrorCode.SCHEDULE_ENDED, "종료된 일정은 수정 불가합니다.");
        }

        // 대면 -> 비대면 전환 시
        if (command.isChangingToOnline()) {
            schedule.convertToOnline();
        }

        // 일반 필드 업데이트 (대면/비대면 유지 또는 비대면 -> 대면 포함)
        schedule.update(
            command.name(),
            command.description(),
            command.tags(),
            command.startsAt(),
            command.endsAt(),
            extractLocationName(command),
            extractLocation(command),
            createPolicyFromCommand(command, schedule)
        );

        if (command.isParticipantsUpdateRequested()) {
            // DB에 있는 기존 참여자 ID 목록을 가져옴
            Set<Long> existingParticipantIds = loadScheduleParticipantPort.findMemberIdsByScheduleId(schedule.getId());

            // 진짜 명단이 달라졌는지 비교
            if (!existingParticipantIds.equals(command.participantMemberIds())) {
                // 진짜 달라졌을 때만 권한 검증 및 업데이트 수행
                updateParticipants(schedule, command);
            }
        }

        // 참여자 update
        updateParticipants(schedule, command);

        // 일정 update
        saveSchedulePort.save(schedule);

        return schedule.getId();
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

    private String extractLocationName(EditScheduleCommand command) {
        return command.location() != null ? command.location().locationName() : null;
    }

    private Point extractLocation(EditScheduleCommand command) {
        if (command.location() == null) {
            return null;
        }
        return GeometryUtils.createPoint(
            command.location().latitude(),
            command.location().longitude()
        );
    }

    // 참여자 업데이트
    private void updateParticipants(Schedule schedule, EditScheduleCommand command) {

        // 참여자 변경 없으면 스킵
        if (!command.isParticipantsUpdateRequested()) {
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
}
