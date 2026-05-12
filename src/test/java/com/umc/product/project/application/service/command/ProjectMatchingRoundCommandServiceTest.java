package com.umc.product.project.application.service.command;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.umc.product.authorization.application.port.in.query.GetChallengerRoleUseCase;
import com.umc.product.authorization.application.port.in.query.dto.ChallengerRoleInfo;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.common.domain.enums.OrganizationType;
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
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ProjectMatchingRoundCommandServiceTest {

    private static final Long ROUND_ID = 1L;
    private static final Long EXECUTOR_MEMBER_ID = 999L;

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

    @InjectMocks
    ProjectMatchingRoundCommandService sut;

    @BeforeEach
    void setUpRoleMock() {
        given(getChallengerRoleUseCase.findAllByMemberId(EXECUTOR_MEMBER_ID))
            .willReturn(List.of(centralCoreRole()));
    }

    @Nested
    class lifecycleHooks {

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
            UpdateProjectMatchingRoundCommand command = updateCommand(ROUND_ID);
            given(loadProjectMatchingRoundPort.getById(ROUND_ID)).willReturn(existing);
            given(loadProjectMatchingRoundPort.listOverlappingExceptId(any(), any(), any(), any()))
                .willReturn(List.of());

            sut.update(command);

            then(scheduleMatchingRoundDeadlinePort).should().schedule(existing);
        }

        @Test
        void delete는_도메인_삭제_후_scheduleMatchingRoundDeadlinePort에_cancel_호출한다() {
            ProjectMatchingRound existing = futureRound(MatchingType.PLAN_DESIGN);
            given(loadProjectMatchingRoundPort.getById(ROUND_ID)).willReturn(existing);
            given(loadProjectApplicationPort.existsByAppliedMatchingRoundId(ROUND_ID)).willReturn(false);

            sut.delete(ROUND_ID, EXECUTOR_MEMBER_ID);

            then(scheduleMatchingRoundDeadlinePort).should().cancel(ROUND_ID);
        }

        @Test
        void delete는_연관_지원서가_있으면_예외_발생하고_cancel_호출되지_않는다() {
            ProjectMatchingRound existing = futureRound(MatchingType.PLAN_DESIGN);
            given(loadProjectMatchingRoundPort.getById(ROUND_ID)).willReturn(existing);
            given(loadProjectApplicationPort.existsByAppliedMatchingRoundId(ROUND_ID)).willReturn(true);

            assertThatThrownBy(() -> sut.delete(ROUND_ID, EXECUTOR_MEMBER_ID))
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.PROJECT_MATCHING_ROUND_DELETE_CONFLICT);
            then(scheduleMatchingRoundDeadlinePort).should(never()).cancel(any());
        }
    }

    private CreateProjectMatchingRoundCommand createCommand(Long chapterId) {
        Instant startsAt = Instant.now().plusSeconds(86_400);
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
            Instant.now().plusSeconds(86_400),
            Instant.now().plusSeconds(172_800),
            Instant.now().plusSeconds(259_200)
        );
        ReflectionTestUtils.setField(round, "id", ROUND_ID);
        return round;
    }

    private ChallengerRoleInfo centralCoreRole() {
        return ChallengerRoleInfo.builder()
            .roleType(ChallengerRoleType.CENTRAL_PRESIDENT)
            .organizationType(OrganizationType.CENTRAL)
            .organizationId(null)
            .gisuId(5L)
            .build();
    }
}
