package com.umc.product.schedule.application.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfoWithStatus;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.organization.application.port.in.query.dto.gisu.GisuInfo;
import com.umc.product.schedule.application.port.in.command.dto.CreateScheduleCommand;
import com.umc.product.schedule.application.port.in.command.dto.EditScheduleCommand;
import com.umc.product.schedule.application.port.in.query.dto.ScheduleCapabilitiesInfo;
import com.umc.product.schedule.application.port.out.DeleteScheduleParticipantPort;
import com.umc.product.schedule.application.port.out.DeleteSchedulePort;
import com.umc.product.schedule.application.port.out.LoadScheduleParticipantPort;
import com.umc.product.schedule.application.port.out.LoadSchedulePort;
import com.umc.product.schedule.application.port.out.SaveScheduleParticipantPort;
import com.umc.product.schedule.application.port.out.SaveSchedulePort;
import com.umc.product.schedule.application.service.query.ScheduleCapabilitiesService;
import com.umc.product.schedule.domain.Schedule;
import com.umc.product.schedule.domain.ScheduleParticipant;
import com.umc.product.schedule.domain.enums.ScheduleTag;
import com.umc.product.schedule.domain.exception.ScheduleDomainException;
import com.umc.product.schedule.domain.exception.ScheduleErrorCode;

@ExtendWith(MockitoExtension.class)
@DisplayName("ScheduleCommandService 출석 일정의 참여자 하한")
class ScheduleCommandServiceParticipantBoundTest {

    private static final Long SCHEDULE_ID = 100L;
    private static final Long AUTHOR_MEMBER_ID = 10L;
    private static final Instant STARTS_AT = Instant.parse("2026-11-02T14:00:00Z");
    private static final Instant ENDS_AT = Instant.parse("2026-11-02T16:00:00Z");

    @Mock
    SaveSchedulePort saveSchedulePort;
    @Mock
    LoadSchedulePort loadSchedulePort;
    @Mock
    DeleteSchedulePort deleteSchedulePort;
    @Mock
    SaveScheduleParticipantPort saveScheduleParticipantPort;
    @Mock
    DeleteScheduleParticipantPort deleteScheduleParticipantPort;
    @Mock
    LoadScheduleParticipantPort loadScheduleParticipantPort;
    @Mock
    ScheduleCapabilitiesService capabilitiesService;
    @Mock
    GetChallengerUseCase getChallengerUseCase;
    @Mock
    GetGisuUseCase getGisuUseCase;
    @Mock
    GetMemberUseCase getMemberUseCase;
    @InjectMocks
    ScheduleCommandService sut;

    // 스터디 출결 규정값: 시작 10분 전부터 요청 가능, 시작 정각까지 출석, 이후 11분까지 지각
    private static CreateScheduleCommand.AttendancePolicyInfo studyPolicy() {
        return CreateScheduleCommand.AttendancePolicyInfo.builder()
            .checkInStartAt(STARTS_AT.minus(10, ChronoUnit.MINUTES))
            .onTimeEndAt(STARTS_AT)
            .lateEndAt(STARTS_AT.plus(11, ChronoUnit.MINUTES))
            .build();
    }

    private static CreateScheduleCommand createCommand(
        CreateScheduleCommand.AttendancePolicyInfo policy,
        Set<Long> participantMemberIds
    ) {
        return CreateScheduleCommand.builder()
            .name("[중앙대-웹PE-1팀] 3주차 스터디")
            .description("스터디")
            .tags(Set.of(ScheduleTag.STUDY))
            .authorMemberId(AUTHOR_MEMBER_ID)
            .startsAt(STARTS_AT)
            .endsAt(ENDS_AT)
            .attendancePolicy(policy)
            .participantMemberIds(participantMemberIds)
            .build();
    }

    // 출석 정책을 가진 기존 일정. 수정 경로는 시작 전 일정만 허용하므로 미래 시각으로 둔다.
    private static Schedule attendanceRequiredSchedule() {
        Instant startsAt = Instant.now().plus(7, ChronoUnit.DAYS);
        Instant endsAt = startsAt.plus(2, ChronoUnit.HOURS);

        Schedule schedule = new Schedule() {
        };
        ReflectionTestUtils.setField(schedule, "id", SCHEDULE_ID);
        ReflectionTestUtils.setField(schedule, "authorMemberId", AUTHOR_MEMBER_ID);
        ReflectionTestUtils.setField(schedule, "startsAt", startsAt);
        ReflectionTestUtils.setField(schedule, "endsAt", endsAt);
        ReflectionTestUtils.setField(schedule, "policy", Schedule.createAttendancePolicy(
            startsAt.minus(10, ChronoUnit.MINUTES), startsAt, startsAt.plus(11, ChronoUnit.MINUTES),
            startsAt, endsAt
        ));
        return schedule;
    }

    // 출석 정책이 없는 기존 일정
    private static Schedule attendanceNotRequiredSchedule() {
        Instant startsAt = Instant.now().plus(7, ChronoUnit.DAYS);

        Schedule schedule = new Schedule() {
        };
        ReflectionTestUtils.setField(schedule, "id", SCHEDULE_ID);
        ReflectionTestUtils.setField(schedule, "authorMemberId", AUTHOR_MEMBER_ID);
        ReflectionTestUtils.setField(schedule, "startsAt", startsAt);
        ReflectionTestUtils.setField(schedule, "endsAt", startsAt.plus(2, ChronoUnit.HOURS));
        return schedule;
    }

    // 참여자 명단은 생략하고 출석 정책만 추가하는 수정 요청
    private static EditScheduleCommand addPolicyWithoutParticipantList() {
        Instant startsAt = Instant.now().plus(7, ChronoUnit.DAYS).truncatedTo(ChronoUnit.MINUTES);

        return EditScheduleCommand.builder()
            .scheduleId(SCHEDULE_ID)
            .startsAt(startsAt)
            .endsAt(startsAt.plus(2, ChronoUnit.HOURS))
            .isAttendanceRequired(true)
            .attendancePolicy(EditScheduleCommand.AttendancePolicyInfo.builder()
                .checkInStartAt(startsAt.minus(10, ChronoUnit.MINUTES))
                .onTimeEndAt(startsAt)
                .lateEndAt(startsAt.plus(11, ChronoUnit.MINUTES))
                .build())
            .build();
    }

    private void givenAuthorCanCreateAttendanceSchedule() {
        given(capabilitiesService.getCapabilities(AUTHOR_MEMBER_ID))
            .willReturn(ScheduleCapabilitiesInfo.forSchoolCore());
    }

    private void givenActiveGisuCoversSchedule() {
        given(getChallengerUseCase.getLatestActiveChallengerByMemberId(AUTHOR_MEMBER_ID))
            .willReturn(ChallengerInfoWithStatus.builder().memberId(AUTHOR_MEMBER_ID).build());
        given(getGisuUseCase.getActiveGisu())
            .willReturn(new GisuInfo(1L, 11L, STARTS_AT.minus(30, ChronoUnit.DAYS),
                ENDS_AT.plus(30, ChronoUnit.DAYS), true));
        given(saveSchedulePort.save(any(Schedule.class)))
            .willAnswer(invocation -> {
                Schedule saved = invocation.getArgument(0);
                ReflectionTestUtils.setField(saved, "id", SCHEDULE_ID);
                return saved;
            });
    }

    @Nested
    @DisplayName("일정 생성")
    class Create {

        @Test
        @DisplayName("출석 정책이 있는데 참여자가 0명이면 PARTICIPANT_REQUIRED 예외가 발생한다")
        void 출석_일정_참여자_0명_거부() {
            // given
            givenAuthorCanCreateAttendanceSchedule();

            // when & then
            assertThatThrownBy(() -> sut.create(createCommand(studyPolicy(), Set.of())))
                .isInstanceOf(ScheduleDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ScheduleErrorCode.PARTICIPANT_REQUIRED);

            then(saveSchedulePort).should(never()).save(any(Schedule.class));
            then(saveScheduleParticipantPort).should(never()).saveAll(anyList());
        }

        @Test
        @DisplayName("출석 정책이 없으면 참여자 0명도 허용한다")
        void 출석_없는_일정_참여자_0명_허용() {
            // given
            givenAuthorCanCreateAttendanceSchedule();
            givenActiveGisuCoversSchedule();

            // when
            Long scheduleId = sut.create(createCommand(null, Set.of()));

            // then
            assertThat(scheduleId).isEqualTo(SCHEDULE_ID);
        }

        @Test
        @DisplayName("유예 0분인 스터디 규정값으로 출석 일정을 생성할 수 있다")
        void 유예_0분_출석_일정_생성() {
            // given
            givenAuthorCanCreateAttendanceSchedule();
            givenActiveGisuCoversSchedule();
            given(getMemberUseCase.countMembersByIds(Set.of(1L, 2L))).willReturn(2L);

            // when
            Long scheduleId = sut.create(createCommand(studyPolicy(), Set.of(1L, 2L)));

            // then
            assertThat(scheduleId).isEqualTo(SCHEDULE_ID);
            then(saveScheduleParticipantPort).should().saveAll(anyList());
        }
    }

    @Nested
    @DisplayName("일정 수정")
    class Update {

        @Test
        @DisplayName("출석 정책이 남아 있는 일정에서 참여자를 전원 제거하면 PARTICIPANT_REQUIRED 예외가 발생한다")
        void 출석_일정_참여자_전원_제거_거부() {
            // given
            given(loadSchedulePort.findById(SCHEDULE_ID))
                .willReturn(Optional.of(attendanceRequiredSchedule()));
            givenAuthorCanCreateAttendanceSchedule();

            EditScheduleCommand command = EditScheduleCommand.builder()
                .scheduleId(SCHEDULE_ID)
                .participantMemberIds(Set.of())
                .build();

            // when & then
            assertThatThrownBy(() -> sut.update(command))
                .isInstanceOf(ScheduleDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ScheduleErrorCode.PARTICIPANT_REQUIRED);

            then(deleteScheduleParticipantPort).should(never()).deleteAll(anyList());
            then(saveSchedulePort).should(never()).save(any(Schedule.class));
        }

        @Test
        @DisplayName("참여자 명단을 생략하고 출석 정책만 추가할 때, 기존 참여자가 없으면 거부한다")
        void 명단_생략_출석_정책_추가_기존_참여자_없음_거부() {
            // given
            given(loadSchedulePort.findById(SCHEDULE_ID))
                .willReturn(Optional.of(attendanceNotRequiredSchedule()));
            givenAuthorCanCreateAttendanceSchedule();
            given(loadScheduleParticipantPort.findMemberIdsByScheduleId(SCHEDULE_ID))
                .willReturn(Set.of());

            // when & then
            assertThatThrownBy(() -> sut.update(addPolicyWithoutParticipantList()))
                .isInstanceOf(ScheduleDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ScheduleErrorCode.PARTICIPANT_REQUIRED);

            then(saveSchedulePort).should(never()).save(any(Schedule.class));
        }

        @Test
        @DisplayName("참여자 명단을 생략하고 출석 정책만 추가할 때, 기존 참여자가 있으면 허용한다")
        void 명단_생략_출석_정책_추가_기존_참여자_있음_허용() {
            // given
            Schedule schedule = attendanceNotRequiredSchedule();
            given(loadSchedulePort.findById(SCHEDULE_ID)).willReturn(Optional.of(schedule));
            givenAuthorCanCreateAttendanceSchedule();
            given(loadScheduleParticipantPort.findMemberIdsByScheduleId(SCHEDULE_ID))
                .willReturn(Set.of(1L));

            // when
            sut.update(addPolicyWithoutParticipantList());

            // then
            assertThat(schedule.getPolicy()).isNotNull();
            assertThat(schedule.getPolicy().getAttendanceGraceMinutes()).isZero();
        }

        @Test
        @DisplayName("출석을 해제하면서 참여자를 전원 제거하는 것은 허용한다")
        void 출석_해제와_함께_참여자_전원_제거_허용() {
            // given
            Schedule schedule = attendanceRequiredSchedule();
            given(loadSchedulePort.findById(SCHEDULE_ID)).willReturn(Optional.of(schedule));
            givenAuthorCanCreateAttendanceSchedule();
            given(loadScheduleParticipantPort.findMemberIdsByScheduleId(SCHEDULE_ID))
                .willReturn(Set.of(1L));
            given(loadScheduleParticipantPort.findAllByScheduleId(SCHEDULE_ID))
                .willReturn(List.of(ScheduleParticipant.builder()
                    .memberId(1L)
                    .schedule(schedule)
                    .attendance(null)
                    .build()));

            EditScheduleCommand command = EditScheduleCommand.builder()
                .scheduleId(SCHEDULE_ID)
                .isAttendanceRequired(false)
                .participantMemberIds(Set.of())
                .build();

            // when
            sut.update(command);

            // then
            assertThat(schedule.getPolicy()).isNull();
            then(deleteScheduleParticipantPort).should().deleteAll(anyList());
        }
    }

    @Nested
    @DisplayName("수정 경로의 출석 정책 시각")
    class UpdatePolicyTime {

        @Test
        @DisplayName("유예 0분 정책으로 수정할 수 있다")
        void 유예_0분_수정_통과() {
            // given
            Schedule schedule = attendanceRequiredSchedule();
            given(loadSchedulePort.findById(SCHEDULE_ID)).willReturn(Optional.of(schedule));
            givenAuthorCanCreateAttendanceSchedule();
            given(loadScheduleParticipantPort.findMemberIdsByScheduleId(SCHEDULE_ID))
                .willReturn(Set.of(1L));

            // when
            sut.update(addPolicyWithoutParticipantList());

            // then
            assertThat(schedule.getPolicy().getAttendanceGraceMinutes()).isZero();
        }

        @Test
        @DisplayName("간격이 정수 분이 아닌 정책으로 수정하면 INVALID_TIME_RANGE 예외가 발생한다")
        void 정수_분_아닌_간격_수정_거부() {
            // given
            given(loadSchedulePort.findById(SCHEDULE_ID))
                .willReturn(Optional.of(attendanceRequiredSchedule()));
            givenAuthorCanCreateAttendanceSchedule();
            given(loadScheduleParticipantPort.findMemberIdsByScheduleId(SCHEDULE_ID))
                .willReturn(Set.of(1L));

            Instant startsAt = Instant.now().plus(7, ChronoUnit.DAYS).truncatedTo(ChronoUnit.MINUTES);
            EditScheduleCommand command = EditScheduleCommand.builder()
                .scheduleId(SCHEDULE_ID)
                .startsAt(startsAt)
                .endsAt(startsAt.plus(2, ChronoUnit.HOURS))
                .isAttendanceRequired(true)
                .attendancePolicy(EditScheduleCommand.AttendancePolicyInfo.builder()
                    .checkInStartAt(startsAt.minus(10, ChronoUnit.MINUTES))
                    .onTimeEndAt(startsAt.plusSeconds(30))
                    .lateEndAt(startsAt.plus(11, ChronoUnit.MINUTES))
                    .build())
                .build();

            // when & then
            assertThatThrownBy(() -> sut.update(command))
                .isInstanceOf(ScheduleDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ScheduleErrorCode.INVALID_TIME_RANGE);
        }
    }
}
