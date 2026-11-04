package com.umc.product.test.application.service.qa;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.authorization.application.port.in.CheckPermissionUseCase;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourcePermission;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.organization.application.port.in.query.dto.gisu.GisuInfo;
import com.umc.product.schedule.application.port.in.command.CreateScheduleParticipantUseCase;
import com.umc.product.schedule.application.port.in.command.CreateScheduleUseCase;
import com.umc.product.schedule.application.port.in.command.UpdateScheduleParticipantUseCase;
import com.umc.product.schedule.application.port.in.command.dto.CreateScheduleCommand;
import com.umc.product.schedule.application.port.in.command.dto.DecideAttendanceCommand;
import com.umc.product.schedule.application.port.in.command.dto.ExcuseScheduleAttendanceCommand;
import com.umc.product.schedule.application.port.in.command.dto.ScheduleAttendanceCommand;
import com.umc.product.schedule.domain.enums.AttendanceStatus;
import com.umc.product.schedule.domain.enums.ScheduleTag;
import com.umc.product.test.application.port.in.command.dto.QaSeedStep;
import com.umc.product.test.application.port.out.QaSeedMarkerPort;
import com.umc.product.test.domain.roster.QaPersonas;
import com.umc.product.test.domain.roster.QaPersonas.Persona;

import lombok.RequiredArgsConstructor;

/**
 * 일정 4개와 출석 상태. 출석 요청이 두 일정에 걸쳐 있어 네 일정을 한 묶음으로 보고, 하나라도 있으면 건너뛴다.
 */
@Component
@Profile("!prod")
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
class QaAttendanceStep implements QaSeedStepRunner {

    private static final List<Persona> PARTICIPANTS = List.of(
        QaPersonas.CAU_PLAN, QaPersonas.CAU_DESIGN, QaPersonas.CAU_WEB, QaPersonas.CAU_MOBILE,
        QaPersonas.CAU_WEB_INFRA, QaPersonas.CAU_MOBILE_INFRA
    );
    private static final List<String> SCHEDULE_TITLES = List.of(
        "예정 일정", "진행 중 출석 확인", "진행 중 지각 확인", "종료 일정");

    private final CreateScheduleUseCase createScheduleUseCase;
    private final CreateScheduleParticipantUseCase createScheduleParticipantUseCase;
    private final UpdateScheduleParticipantUseCase updateScheduleParticipantUseCase;
    private final GetGisuUseCase getGisuUseCase;
    private final CheckPermissionUseCase checkPermissionUseCase;
    private final QaSeedMarkerPort qaSeedMarkerPort;

    @Override
    public QaSeedStep step() {
        return QaSeedStep.ATTENDANCE;
    }

    @Override
    @Transactional
    public StepTally run(QaDirectory directory) {
        StepTally tally = new StepTally();
        List<String> names = SCHEDULE_TITLES.stream().map(QaAttendanceStep::name).toList();
        if (!qaSeedMarkerPort.findExistingScheduleNames(names).isEmpty()) {
            tally.skipped(SCHEDULE_TITLES.size());
            return tally;
        }
        seed(directory);
        tally.created(SCHEDULE_TITLES.size());
        return tally;
    }

    private void seed(QaDirectory directory) {
        Long authorId = directory.memberId(QaPersonas.CAU_SCHOOL_PRESIDENT);
        Set<Long> participantIds = PARTICIPANTS.stream().map(directory::memberId).collect(Collectors.toSet());
        Instant base = Instant.now();

        schedule(directory, "예정 일정", authorId, participantIds,
            base.plus(24, ChronoUnit.HOURS), base.plus(26, ChronoUnit.HOURS),
            base.plus(24, ChronoUnit.HOURS).plus(10, ChronoUnit.MINUTES),
            base.plus(25, ChronoUnit.HOURS));
        Long presentScheduleId = schedule(directory, "진행 중 출석 확인", authorId, participantIds,
            base.minus(30, ChronoUnit.MINUTES), base.plus(3, ChronoUnit.HOURS),
            base.plus(1, ChronoUnit.HOURS), base.plus(2, ChronoUnit.HOURS));
        Long lateScheduleId = schedule(directory, "진행 중 지각 확인", authorId, participantIds,
            base.minus(2, ChronoUnit.HOURS), base.plus(3, ChronoUnit.HOURS),
            base.minus(1, ChronoUnit.HOURS), base.plus(2, ChronoUnit.HOURS));
        schedule(directory, "종료 일정", authorId, participantIds,
            base.minus(3, ChronoUnit.HOURS), base.minus(2, ChronoUnit.HOURS),
            base.minus(3, ChronoUnit.HOURS).plus(10, ChronoUnit.MINUTES),
            base.minus(3, ChronoUnit.HOURS).plus(20, ChronoUnit.MINUTES));

        attendance(presentScheduleId, directory.memberId(QaPersonas.CAU_PLAN), authorId, true,
            AttendanceStatus.PRESENT_PENDING);
        attendance(presentScheduleId, directory.memberId(QaPersonas.CAU_DESIGN), authorId, false,
            AttendanceStatus.PRESENT_PENDING);
        attendance(lateScheduleId, directory.memberId(QaPersonas.CAU_WEB), authorId, true,
            AttendanceStatus.LATE_PENDING);

        Long excusedMemberId = directory.memberId(QaPersonas.CAU_MOBILE);
        checkPermissionUseCase.checkOrThrow(excusedMemberId,
            ResourcePermission.of(ResourceType.ATTENDANCE, presentScheduleId, PermissionType.WRITE));
        createScheduleParticipantUseCase.createExcusedScheduleParticipantWithAttendance(
            ExcuseScheduleAttendanceCommand.builder()
                .scheduleId(presentScheduleId).requesterMemberId(excusedMemberId).isVerified(false)
                .excuseReason("[QA] 수업 일정 중복으로 출석 사유를 제출합니다.").build());
    }

    private Long schedule(QaDirectory directory, String title, Long authorId, Set<Long> participants,
                          Instant startsAt, Instant endsAt, Instant onTimeEndAt, Instant lateEndAt) {
        GisuInfo activeGisu = getGisuUseCase.getActiveGisu();
        if (!activeGisu.gisuId().equals(directory.gisuId(11))
            || startsAt.isBefore(activeGisu.startAt()) || endsAt.isAfter(activeGisu.endAt())) {
            throw new IllegalStateException("QA 출석 일정은 활성 11기 활동 기간 안에서만 생성할 수 있습니다.");
        }
        return createScheduleUseCase.create(CreateScheduleCommand.builder()
            .name(name(title)).description("고정 QA 계정의 일정·출석 검증용 비대면 일정입니다.")
            .tags(Set.of(ScheduleTag.GENERAL)).authorMemberId(authorId).startsAt(startsAt).endsAt(endsAt)
            .attendancePolicy(CreateScheduleCommand.AttendancePolicyInfo.builder()
                .checkInStartAt(startsAt.minus(10, ChronoUnit.MINUTES))
                .onTimeEndAt(onTimeEndAt).lateEndAt(lateEndAt).build())
            .participantMemberIds(participants).build());
    }

    private void attendance(Long scheduleId, Long memberId, Long authorId, boolean approved,
                            AttendanceStatus expectedPendingStatus) {
        checkPermissionUseCase.checkOrThrow(memberId,
            ResourcePermission.of(ResourceType.ATTENDANCE, scheduleId, PermissionType.WRITE));
        var result = createScheduleParticipantUseCase.createScheduleParticipantWithAttendance(
            ScheduleAttendanceCommand.builder().scheduleId(scheduleId).requesterMemberId(memberId)
                .locationVerified(false).build());
        if (result.status() != expectedPendingStatus) {
            throw new IllegalStateException("QA 출석 요청 시간대가 지났습니다.");
        }
        checkPermissionUseCase.checkOrThrow(authorId,
            ResourcePermission.of(ResourceType.ATTENDANCE, scheduleId, PermissionType.APPROVE));
        updateScheduleParticipantUseCase.decideAttendances(List.of(DecideAttendanceCommand.builder()
            .scheduleId(scheduleId).decidedByMemberId(authorId).participantMemberId(memberId)
            .isApproved(approved).reason(approved ? "[QA] 출석 요청 승인" : "[QA] 출석 요청 거절로 결석 상태 확인")
            .build()));
    }

    private static String name(String title) {
        return "[QA] " + title;
    }
}
