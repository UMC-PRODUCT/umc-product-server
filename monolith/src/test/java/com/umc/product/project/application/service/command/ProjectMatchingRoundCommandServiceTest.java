package com.umc.product.project.application.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.umc.product.authorization.application.port.in.query.GetChallengerRoleUseCase;
import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import com.umc.product.project.application.port.in.command.dto.CreateProjectMatchingRoundCommand;
import com.umc.product.project.application.port.in.command.dto.UpdateProjectMatchingRoundCommand;
import com.umc.product.project.application.port.out.LoadProjectApplicationPort;
import com.umc.product.project.application.port.out.LoadProjectMatchingRoundPort;
import com.umc.product.project.application.port.out.SaveProjectMatchingRoundPort;
import com.umc.product.project.application.port.out.ScheduleMatchingRoundDeadlinePort;
import com.umc.product.project.domain.ProjectMatchingRound;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;

@ExtendWith(MockitoExtension.class)
class ProjectMatchingRoundCommandServiceTest {

    private static final Long ROUND_ID = 1L;
    private static final Long EXECUTOR_MEMBER_ID = 999L;
    private static final Clock TEST_CLOCK = Clock.fixed(Instant.parse("2026-05-01T00:00:00Z"), ZoneOffset.UTC);

    @Mock
    LoadProjectMatchingRoundPort loadProjectMatchingRoundPort;
    @Mock
    SaveProjectMatchingRoundPort saveProjectMatchingRoundPort;
    @Mock
    LoadProjectApplicationPort loadProjectApplicationPort;
    @Mock
    ScheduleMatchingRoundDeadlinePort scheduleMatchingRoundDeadlinePort;
    @Mock
    GetChallengerRoleUseCase getChallengerRoleUseCase;

    @Mock
    GetChapterUseCase getChapterUseCase;

    ProjectMatchingRoundCommandService sut;

    @BeforeEach
    void setUpRoleMock() {
        sut = sutWithMinPhaseIntervalMinutes(1);
        given(getChapterUseCase.getGisuIdByChapterId(1L)).willReturn(5L);
        given(getChallengerRoleUseCase.isCentralCoreInGisu(EXECUTOR_MEMBER_ID, 5L)).willReturn(true);
    }

    @Nested
    class resultPublishAt {

        @Test
        void 선발_마감만_발표_시각_이후로_수정하면_거부한다() {
            // given
            ProjectMatchingRound existing = futureRound(MatchingType.PLAN_DESIGN);
            Instant resultPublishAt = existing.getResultPublishAt();
            Instant originalDeadline = existing.getDecisionDeadline();
            given(loadProjectMatchingRoundPort.getByIdForUpdate(ROUND_ID)).willReturn(existing);
            UpdateProjectMatchingRoundCommand command = UpdateProjectMatchingRoundCommand.builder()
                .matchingRoundId(ROUND_ID)
                .requesterMemberId(EXECUTOR_MEMBER_ID)
                .decisionDeadline(resultPublishAt.plusSeconds(1))
                .build();

            // when & then
            assertThatThrownBy(() -> sut.update(command))
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.PROJECT_MATCHING_ROUND_INVALID_PERIOD);
            assertThat(existing.getDecisionDeadline()).isEqualTo(originalDeadline);
            assertThat(existing.getResultPublishAt()).isEqualTo(resultPublishAt);
            then(scheduleMatchingRoundDeadlinePort).should(never()).schedule(any());
        }
    }

    @Nested
    class lifecycleHooks {

        @Test
        void 랜덤_매칭은_종료와_마감을_같게_등록하고_지원서를_받지_않는다() {
            Instant startsAt = Instant.parse("2026-11-19T00:00:00+09:00");
            CreateProjectMatchingRoundCommand command = CreateProjectMatchingRoundCommand.builder()
                .requesterMemberId(EXECUTOR_MEMBER_ID).name("개발자 랜덤 매칭")
                .type(MatchingType.PLAN_DEVELOPER).phase(MatchingPhase.RANDOM_MATCHING).chapterId(1L)
                .startsAt(startsAt).endsAt(startsAt.plusSeconds(72000))
                .decisionDeadline(startsAt.plusSeconds(72000)).resultPublishAt(startsAt.plusSeconds(79200))
                .build();
            given(saveProjectMatchingRoundPort.save(any())).willAnswer(invocation -> {
                ProjectMatchingRound saved = invocation.getArgument(0);
                ReflectionTestUtils.setField(saved, "id", ROUND_ID);
                return saved;
            });

            assertThat(sut.create(command)).isEqualTo(ROUND_ID);

            ArgumentCaptor<ProjectMatchingRound> captor = ArgumentCaptor.forClass(ProjectMatchingRound.class);
            then(scheduleMatchingRoundDeadlinePort).should().schedule(captor.capture());
            ProjectMatchingRound saved = captor.getValue();
            assertThat(saved.isOpenAt(startsAt)).isFalse();
            assertThat(saved.getEndsAt()).isEqualTo(saved.getDecisionDeadline());
        }

        @Test
        void 자동_선발이_완료된_차수는_삭제할_수_없다() {
            ProjectMatchingRound round = futureRound(MatchingType.PLAN_DEVELOPER);
            round.executeAutoDecision(null);
            given(loadProjectMatchingRoundPort.getByIdForUpdate(ROUND_ID)).willReturn(round);

            assertThatThrownBy(() -> sut.delete(ROUND_ID, EXECUTOR_MEMBER_ID))
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.PROJECT_MATCHING_ROUND_LOCKED);
            then(saveProjectMatchingRoundPort).should(never()).delete(any());
        }

        @Test
        void 대상_기수와_지부의_관리_권한이_없으면_생성을_거부한다() {
            reset(getChallengerRoleUseCase);
            given(getChapterUseCase.getGisuIdByChapterId(1L)).willReturn(6L);

            assertThatThrownBy(() -> sut.create(createCommand(1L)))
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.PROJECT_MATCHING_ROUND_ACCESS_DENIED);

            then(getChallengerRoleUseCase).should().isCentralCoreInGisu(EXECUTOR_MEMBER_ID, 6L);
            then(getChallengerRoleUseCase).should().isChapterPresidentInGisu(EXECUTOR_MEMBER_ID, 6L, 1L);
            then(saveProjectMatchingRoundPort).should(never()).save(any());
        }

        @Test
        void system_role_super_admin은_challenger_role_없이_차수를_생성한다() {
            reset(getChallengerRoleUseCase);
            CreateProjectMatchingRoundCommand command = createCommand(1L);
            ProjectMatchingRound saved = futureRound(MatchingType.PLAN_DESIGN);
            given(getChallengerRoleUseCase.isSuperAdmin(EXECUTOR_MEMBER_ID)).willReturn(true);
            given(loadProjectMatchingRoundPort.listOverlapping(any(), any(), any())).willReturn(List.of());
            given(saveProjectMatchingRoundPort.save(any())).willReturn(saved);

            sut.create(command);

            then(getChallengerRoleUseCase).should(never()).isCentralCoreInGisu(EXECUTOR_MEMBER_ID, 5L);
            then(saveProjectMatchingRoundPort).should().save(any());
        }

        @Test
        void create는_저장_후_scheduleMatchingRoundDeadlinePort에_schedule_호출한다() {
            CreateProjectMatchingRoundCommand command = createCommand(1L);
            ProjectMatchingRound saved = futureRound(MatchingType.PLAN_DESIGN);
            given(loadProjectMatchingRoundPort.listOverlapping(any(), any(), any())).willReturn(List.of());
            given(saveProjectMatchingRoundPort.save(any())).willReturn(saved);

            sut.create(command);

            then(scheduleMatchingRoundDeadlinePort).should().schedule(saved);
        }

        @Test
        void update는_도메인_갱신_후_scheduleMatchingRoundDeadlinePort에_schedule_호출한다() {
            ProjectMatchingRound existing = futureRound(MatchingType.PLAN_DESIGN);
            Instant resultPublishAt = existing.getResultPublishAt();
            UpdateProjectMatchingRoundCommand command = updateCommand(ROUND_ID);
            given(loadProjectMatchingRoundPort.getByIdForUpdate(ROUND_ID)).willReturn(existing);
            given(loadProjectMatchingRoundPort.listOverlappingExceptId(any(), any(), any(), any()))
                .willReturn(List.of());

            sut.update(command);

            then(scheduleMatchingRoundDeadlinePort).should().schedule(existing);
            assertThat(existing.getResultPublishAt()).isEqualTo(resultPublishAt);
        }

        @Test
        void delete는_도메인_삭제_후_scheduleMatchingRoundDeadlinePort에_cancel_호출한다() {
            ProjectMatchingRound existing = futureRound(MatchingType.PLAN_DESIGN);
            given(loadProjectMatchingRoundPort.getByIdForUpdate(ROUND_ID)).willReturn(existing);
            given(loadProjectApplicationPort.existsByAppliedMatchingRoundId(ROUND_ID)).willReturn(false);

            sut.delete(ROUND_ID, EXECUTOR_MEMBER_ID);

            then(scheduleMatchingRoundDeadlinePort).should().cancel(ROUND_ID);
        }

        @Test
        void delete는_연관_지원서가_있으면_예외_발생하고_cancel_호출되지_않는다() {
            ProjectMatchingRound existing = futureRound(MatchingType.PLAN_DESIGN);
            given(loadProjectMatchingRoundPort.getByIdForUpdate(ROUND_ID)).willReturn(existing);
            given(loadProjectApplicationPort.existsByAppliedMatchingRoundId(ROUND_ID)).willReturn(true);

            assertThatThrownBy(() -> sut.delete(ROUND_ID, EXECUTOR_MEMBER_ID))
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.PROJECT_MATCHING_ROUND_DELETE_CONFLICT);
            then(scheduleMatchingRoundDeadlinePort).should(never()).cancel(any());
        }

        @Test
        @DisplayName("이전 차수 decisionDeadline 이후 다음 차수 startsAt까지 1분 미만이면 생성할 수 없다")
        void 이전_차수와_다음_차수_간격이_1분_미만이면_생성할_수_없다() {
            ProjectMatchingRound firstRound = matchingRound(
                10L,
                MatchingType.PLAN_DESIGN,
                MatchingPhase.FIRST,
                1L,
                "2026-05-10T00:00:00Z",
                "2026-05-12T00:00:00Z",
                "2026-05-13T00:00:00Z"
            );
            CreateProjectMatchingRoundCommand command = createCommand(
                1L,
                MatchingType.PLAN_DESIGN,
                MatchingPhase.SECOND,
                "2026-05-13T00:00:30Z",
                "2026-05-15T00:00:00Z",
                "2026-05-16T00:00:00Z"
            );
            given(loadProjectMatchingRoundPort.listOverlapping(any(), any(), any())).willReturn(List.of());
            given(loadProjectMatchingRoundPort.listByChapterId(1L)).willReturn(List.of(firstRound));

            assertThatThrownBy(() -> sut.create(command))
                .isInstanceOf(ProjectDomainException.class)
                .extracting(e -> ((ProjectDomainException) e).getBaseCode().getCode())
                .isEqualTo("PROJECT-0309");
            then(saveProjectMatchingRoundPort).should(never()).save(any());
        }

        @Test
        @DisplayName("설정된 분 단위 간격보다 짧으면 생성할 수 없다")
        void 설정된_분_단위_간격보다_짧으면_생성할_수_없다() {
            sut = sutWithMinPhaseIntervalMinutes(2);
            ProjectMatchingRound firstRound = matchingRound(
                10L,
                MatchingType.PLAN_DESIGN,
                MatchingPhase.FIRST,
                1L,
                "2026-05-10T00:00:00Z",
                "2026-05-12T00:00:00Z",
                "2026-05-13T00:00:00Z"
            );
            CreateProjectMatchingRoundCommand command = createCommand(
                1L,
                MatchingType.PLAN_DESIGN,
                MatchingPhase.SECOND,
                "2026-05-13T00:01:30Z",
                "2026-05-15T00:00:00Z",
                "2026-05-16T00:00:00Z"
            );
            given(loadProjectMatchingRoundPort.listOverlapping(any(), any(), any())).willReturn(List.of());
            given(loadProjectMatchingRoundPort.listByChapterId(1L)).willReturn(List.of(firstRound));

            assertThatThrownBy(() -> sut.create(command))
                .isInstanceOf(ProjectDomainException.class)
                .extracting(e -> ((ProjectDomainException) e).getBaseCode().getCode())
                .isEqualTo("PROJECT-0309");
            then(saveProjectMatchingRoundPort).should(never()).save(any());
        }

        @Test
        @DisplayName("같은 지부와 타입에 동일한 차수가 이미 있으면 생성할 수 없다")
        void 같은_지부와_타입에_동일한_차수가_이미_있으면_생성할_수_없다() {
            ProjectMatchingRound existingRound = matchingRound(
                10L,
                MatchingType.PLAN_DESIGN,
                MatchingPhase.FIRST,
                1L,
                "2026-05-10T00:00:00Z",
                "2026-05-12T00:00:00Z",
                "2026-05-13T00:00:00Z"
            );
            CreateProjectMatchingRoundCommand command = createCommand(
                1L,
                MatchingType.PLAN_DESIGN,
                MatchingPhase.FIRST,
                "2026-05-20T00:00:00Z",
                "2026-05-22T00:00:00Z",
                "2026-05-23T00:00:00Z"
            );
            given(loadProjectMatchingRoundPort.listOverlapping(any(), any(), any())).willReturn(List.of());
            given(loadProjectMatchingRoundPort.listByChapterId(1L)).willReturn(List.of(existingRound));

            assertThatThrownBy(() -> sut.create(command))
                .isInstanceOf(ProjectDomainException.class)
                .extracting(e -> ((ProjectDomainException) e).getBaseCode().getCode())
                .isEqualTo("PROJECT-0309");
            then(saveProjectMatchingRoundPort).should(never()).save(any());
        }

        @Test
        @DisplayName("이전 차수 decisionDeadline 이후 다음 차수 startsAt까지 1분 미만이면 수정할 수 없다")
        void 이전_차수와_다음_차수_간격이_1분_미만이면_수정할_수_없다() {
            ProjectMatchingRound firstRound = matchingRound(
                10L,
                MatchingType.PLAN_DESIGN,
                MatchingPhase.FIRST,
                1L,
                "2026-05-10T00:00:00Z",
                "2026-05-12T00:00:00Z",
                "2026-05-13T00:00:00Z"
            );
            ProjectMatchingRound secondRound = matchingRound(
                ROUND_ID,
                MatchingType.PLAN_DESIGN,
                MatchingPhase.SECOND,
                1L,
                "2026-05-20T00:00:00Z",
                "2026-05-22T00:00:00Z",
                "2026-05-23T00:00:00Z"
            );
            UpdateProjectMatchingRoundCommand command = UpdateProjectMatchingRoundCommand.builder()
                .matchingRoundId(ROUND_ID)
                .requesterMemberId(EXECUTOR_MEMBER_ID)
                .startsAt(Instant.parse("2026-05-13T00:00:30Z"))
                .endsAt(Instant.parse("2026-05-15T00:00:00Z"))
                .decisionDeadline(Instant.parse("2026-05-16T00:00:00Z"))
                .build();
            given(loadProjectMatchingRoundPort.getByIdForUpdate(ROUND_ID)).willReturn(secondRound);
            given(loadProjectMatchingRoundPort.listOverlappingExceptId(any(), any(), any(), any()))
                .willReturn(List.of());
            given(loadProjectMatchingRoundPort.listByChapterId(1L)).willReturn(List.of(firstRound, secondRound));

            assertThatThrownBy(() -> sut.update(command))
                .isInstanceOf(ProjectDomainException.class)
                .extracting(e -> ((ProjectDomainException) e).getBaseCode().getCode())
                .isEqualTo("PROJECT-0309");
            then(scheduleMatchingRoundDeadlinePort).should(never()).schedule(any());
        }

        @Test
        @DisplayName("같은 지부와 타입의 기존 차수와 동일한 차수로 수정할 수 없다")
        void 같은_지부와_타입의_기존_차수와_동일한_차수로_수정할_수_없다() {
            ProjectMatchingRound existingRound = matchingRound(
                10L,
                MatchingType.PLAN_DESIGN,
                MatchingPhase.FIRST,
                1L,
                "2026-05-10T00:00:00Z",
                "2026-05-12T00:00:00Z",
                "2026-05-13T00:00:00Z"
            );
            ProjectMatchingRound secondRound = matchingRound(
                ROUND_ID,
                MatchingType.PLAN_DESIGN,
                MatchingPhase.SECOND,
                1L,
                "2026-05-20T00:00:00Z",
                "2026-05-22T00:00:00Z",
                "2026-05-23T00:00:00Z"
            );
            UpdateProjectMatchingRoundCommand command = UpdateProjectMatchingRoundCommand.builder()
                .matchingRoundId(ROUND_ID)
                .requesterMemberId(EXECUTOR_MEMBER_ID)
                .phase(MatchingPhase.FIRST)
                .build();
            given(loadProjectMatchingRoundPort.getByIdForUpdate(ROUND_ID)).willReturn(secondRound);
            given(loadProjectMatchingRoundPort.listOverlappingExceptId(any(), any(), any(), any()))
                .willReturn(List.of());
            given(loadProjectMatchingRoundPort.listByChapterId(1L)).willReturn(List.of(existingRound, secondRound));

            assertThatThrownBy(() -> sut.update(command))
                .isInstanceOf(ProjectDomainException.class)
                .extracting(e -> ((ProjectDomainException) e).getBaseCode().getCode())
                .isEqualTo("PROJECT-0309");
            then(scheduleMatchingRoundDeadlinePort).should(never()).schedule(any());
        }
    }

    @Test
    void 다음_차수는_이전_차수_발표_시각_전에는_시작할_수_없다() {
        // given
        ProjectMatchingRound first = matchingRound(10L, MatchingType.PLAN_DESIGN, MatchingPhase.FIRST, 1L,
            "2026-05-10T00:00:00Z", "2026-05-12T00:00:00Z", "2026-05-13T00:00:00Z");
        CreateProjectMatchingRoundCommand command = createCommand(1L, MatchingType.PLAN_DESIGN, MatchingPhase.SECOND,
            "2026-05-13T01:59:59Z", "2026-05-15T00:00:00Z", "2026-05-16T00:00:00Z");
        given(loadProjectMatchingRoundPort.listByChapterId(1L)).willReturn(List.of(first));

        // when & then
        assertThatThrownBy(() -> sut.create(command))
            .isInstanceOf(ProjectDomainException.class)
            .extracting("baseCode")
            .isEqualTo(ProjectErrorCode.PROJECT_MATCHING_ROUND_PHASE_SEQUENCE_INVALID);
        then(saveProjectMatchingRoundPort).should(never()).save(any());
    }

    @Test
    void 다음_차수는_이전_차수_발표_시각부터_시작할_수_있다() {
        // given
        ProjectMatchingRound first = matchingRound(10L, MatchingType.PLAN_DESIGN, MatchingPhase.FIRST, 1L,
            "2026-05-10T00:00:00Z", "2026-05-12T00:00:00Z", "2026-05-13T00:00:00Z");
        CreateProjectMatchingRoundCommand command = createCommand(1L, MatchingType.PLAN_DESIGN, MatchingPhase.SECOND,
            "2026-05-13T02:00:00Z", "2026-05-15T00:00:00Z", "2026-05-16T00:00:00Z");
        given(loadProjectMatchingRoundPort.listByChapterId(1L)).willReturn(List.of(first));
        given(saveProjectMatchingRoundPort.save(any())).willAnswer(invocation -> {
            ProjectMatchingRound saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", ROUND_ID);
            return saved;
        });

        // when
        Long createdId = sut.create(command);

        // then
        assertThat(createdId).isEqualTo(ROUND_ID);
        ArgumentCaptor<ProjectMatchingRound> captor = ArgumentCaptor.forClass(ProjectMatchingRound.class);
        then(saveProjectMatchingRoundPort).should().save(captor.capture());
        ProjectMatchingRound created = captor.getValue();
        assertThat(created.getPhase()).isEqualTo(MatchingPhase.SECOND);
        assertThat(created.getStartsAt()).isEqualTo(first.getResultPublishAt());
        then(scheduleMatchingRoundDeadlinePort).should().schedule(created);
    }

    @Test
    void 이전_차수_발표를_다음_차수_시작_뒤로_수정할_수_없다() {
        // given
        ProjectMatchingRound first = futureRound(MatchingType.PLAN_DESIGN);
        ProjectMatchingRound second = ProjectMatchingRound.create("2차", null, MatchingType.PLAN_DESIGN,
            MatchingPhase.SECOND, 1L, first.getResultPublishAt(), first.getResultPublishAt().plusSeconds(86400),
            first.getResultPublishAt().plusSeconds(172800), first.getResultPublishAt().plusSeconds(180000));
        ReflectionTestUtils.setField(second, "id", 2L);
        given(loadProjectMatchingRoundPort.getByIdForUpdate(ROUND_ID)).willReturn(first);
        given(loadProjectMatchingRoundPort.listByChapterId(1L)).willReturn(List.of(first, second));
        UpdateProjectMatchingRoundCommand command = UpdateProjectMatchingRoundCommand.builder()
            .matchingRoundId(ROUND_ID).requesterMemberId(EXECUTOR_MEMBER_ID)
            .resultPublishAt(second.getStartsAt().plusSeconds(1)).build();

        // when / then
        assertThatThrownBy(() -> sut.update(command)).isInstanceOf(ProjectDomainException.class)
            .extracting("baseCode").isEqualTo(ProjectErrorCode.PROJECT_MATCHING_ROUND_PHASE_SEQUENCE_INVALID);
        then(scheduleMatchingRoundDeadlinePort).should(never()).schedule(any());
    }

    @Nested
    class WholeSchedule {
        @Test
        @DisplayName("1차 시작 시각부터 전체 일정을 수정할 수 없다")
        void 일차_시작_후_수정_불가() {
            // given
            List<ProjectMatchingRound> existing = existingRounds();
            given(loadProjectMatchingRoundPort.listByChapterId(1L)).willReturn(existing);

            // when & then
            for (long seconds : List.of(0L, 1L)) {
                ReflectionTestUtils.setField(sut, "clock",
                    Clock.fixed(existing.getFirst().getStartsAt().plusSeconds(seconds), ZoneOffset.UTC));
                assertThatThrownBy(() -> sut.saveAll(commands(86400)))
                    .isInstanceOf(ProjectDomainException.class).extracting("baseCode")
                    .isEqualTo(ProjectErrorCode.PROJECT_MATCHING_ROUND_ALREADY_STARTED);
            }
        }

        @Test
        @DisplayName("마지막 차수의 발표 간격 오류가 있으면 기존 일정도 변경하지 않는다")
        void 잘못된_발표_간격_거부() {
            // given
            List<ProjectMatchingRound> existing = existingRounds();
            List<Instant> originalStartsAt = existing.stream().map(ProjectMatchingRound::getStartsAt).toList();
            given(loadProjectMatchingRoundPort.listByChapterId(1L)).willReturn(existing);
            List<CreateProjectMatchingRoundCommand> commands = new ArrayList<>(commands(86400));
            CreateProjectMatchingRoundCommand last = commands.removeLast();
            commands.add(new CreateProjectMatchingRoundCommand(EXECUTOR_MEMBER_ID, last.name(), null, last.type(),
                last.phase(), 1L, last.startsAt(), last.endsAt(), last.decisionDeadline(),
                last.decisionDeadline().plusSeconds(299)));

            // when & then
            assertThatThrownBy(() -> sut.saveAll(commands))
                .isInstanceOf(ProjectDomainException.class).extracting("baseCode")
                .isEqualTo(ProjectErrorCode.PROJECT_MATCHING_ROUND_INVALID_PERIOD);
            assertThat(existing).extracting(ProjectMatchingRound::getStartsAt)
                .containsExactlyElementsOf(originalStartsAt);
        }

        private List<CreateProjectMatchingRoundCommand> commands(long shiftSeconds) {
            List<CreateProjectMatchingRoundCommand> result = new ArrayList<>();
            for (MatchingPhase phase : MatchingPhase.values()) {
                Instant start = Instant.parse("2099-01-01T00:00:00Z")
                    .plusSeconds(phase.ordinal() * 86400L + shiftSeconds);
                Instant end = start.plusSeconds(3600);
                Instant deadline = phase == MatchingPhase.RANDOM_MATCHING ? end : end.plusSeconds(3600);
                result.add(new CreateProjectMatchingRoundCommand(EXECUTOR_MEMBER_ID, phase.name(), null,
                    MatchingType.PLAN_DESIGN, phase, 1L, start, end, deadline, deadline.plusSeconds(300)));
            }
            return result;
        }

        private List<ProjectMatchingRound> existingRounds() {
            List<ProjectMatchingRound> result = new ArrayList<>();
            for (CreateProjectMatchingRoundCommand command : commands(0)) {
                ProjectMatchingRound round = ProjectMatchingRound.create(command.name(), null, command.type(),
                    command.phase(), 1L, command.startsAt(), command.endsAt(),
                    command.decisionDeadline(), command.resultPublishAt());
                result.add(round);
            }
            return result;
        }
    }

    private CreateProjectMatchingRoundCommand createCommand(Long chapterId) {
        Instant startsAt = TEST_CLOCK.instant().plusSeconds(86_400);
        Instant endsAt = startsAt.plusSeconds(86_400);
        Instant decisionDeadline = endsAt.plusSeconds(86_400);
        return CreateProjectMatchingRoundCommand.builder()
            .name("기획-디자인 1차")
            .description(null)
            .type(MatchingType.PLAN_DESIGN)
            .phase(MatchingPhase.FIRST)
            .chapterId(chapterId)
            .startsAt(startsAt)
            .endsAt(endsAt)
            .decisionDeadline(decisionDeadline)
            .resultPublishAt(decisionDeadline.plusSeconds(7200))
            .requesterMemberId(EXECUTOR_MEMBER_ID)
            .build();
    }

    private CreateProjectMatchingRoundCommand createCommand(
        Long chapterId,
        MatchingType type,
        MatchingPhase phase,
        String startsAt,
        String endsAt,
        String decisionDeadline
    ) {
        return CreateProjectMatchingRoundCommand.builder()
            .name("기획-디자인 2차")
            .description(null)
            .type(type)
            .phase(phase)
            .chapterId(chapterId)
            .startsAt(Instant.parse(startsAt))
            .endsAt(Instant.parse(endsAt))
            .decisionDeadline(Instant.parse(decisionDeadline))
            .resultPublishAt(Instant.parse(decisionDeadline).plusSeconds(7200))
            .requesterMemberId(EXECUTOR_MEMBER_ID)
            .build();
    }

    private UpdateProjectMatchingRoundCommand updateCommand(Long roundId) {
        return UpdateProjectMatchingRoundCommand.builder()
            .matchingRoundId(roundId)
            .name("이름 수정")
            .description(null)
            .type(null)
            .phase(null)
            .startsAt(null)
            .endsAt(null)
            .decisionDeadline(null)
            .requesterMemberId(EXECUTOR_MEMBER_ID)
            .build();
    }

    private ProjectMatchingRound futureRound(MatchingType type) {
        ProjectMatchingRound round = ProjectMatchingRound.create(
            "테스트 매칭", null,
            type, MatchingPhase.FIRST, 1L,
            TEST_CLOCK.instant().plusSeconds(86_400),
            TEST_CLOCK.instant().plusSeconds(172_800),
            TEST_CLOCK.instant().plusSeconds(259_200),
            TEST_CLOCK.instant().plusSeconds(266_400)
        );
        ReflectionTestUtils.setField(round, "id", ROUND_ID);
        return round;
    }

    private ProjectMatchingRound matchingRound(
        Long id,
        MatchingType type,
        MatchingPhase phase,
        Long chapterId,
        String startsAt,
        String endsAt,
        String decisionDeadline
    ) {
        ProjectMatchingRound round = ProjectMatchingRound.create(
            "테스트 매칭",
            null,
            type,
            phase,
            chapterId,
            Instant.parse(startsAt),
            Instant.parse(endsAt),
            Instant.parse(decisionDeadline),
            Instant.parse(decisionDeadline).plusSeconds(7200)
        );
        ReflectionTestUtils.setField(round, "id", id);
        return round;
    }

    private ProjectMatchingRoundCommandService sutWithMinPhaseIntervalMinutes(long minPhaseIntervalMinutes) {
        return new ProjectMatchingRoundCommandService(
            loadProjectMatchingRoundPort,
            saveProjectMatchingRoundPort,
            loadProjectApplicationPort,
            scheduleMatchingRoundDeadlinePort,
            getChallengerRoleUseCase,
            new ProjectMatchingRoundProperties(minPhaseIntervalMinutes),
            getChapterUseCase,
            TEST_CLOCK
        );
    }

}
