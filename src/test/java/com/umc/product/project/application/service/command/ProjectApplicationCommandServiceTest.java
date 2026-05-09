package com.umc.product.project.application.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.project.application.port.in.command.dto.ApplicationDecisionStatus;
import com.umc.product.project.application.port.in.query.dto.ProjectApplicationInfo;
import com.umc.product.project.application.port.out.LoadProjectApplicationFormPort;
import com.umc.product.project.application.port.out.LoadProjectApplicationPort;
import com.umc.product.project.application.port.out.LoadProjectMatchingRoundPort;
import com.umc.product.project.application.port.out.LoadProjectMemberPort;
import com.umc.product.project.application.port.out.LoadProjectPartQuotaPort;
import com.umc.product.project.application.port.out.SaveProjectApplicationPort;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectApplication;
import com.umc.product.project.domain.ProjectApplicationForm;
import com.umc.product.project.domain.ProjectMatchingRound;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.enums.ProjectApplicationStatus;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;
import com.umc.product.survey.application.port.in.command.ManageFormResponseUseCase;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ProjectApplicationCommandServiceTest {

    private static final Long APPLICATION_ID = 500L;
    private static final Long APPLICANT_MEMBER_ID = 100L;
    private static final Long DECIDER_MEMBER_ID = 200L;

    private static final Instant NOW = Instant.now();
    private static final Instant ROUND_STARTS_AT = NOW.minusSeconds(86_400);
    private static final Instant ROUND_ENDS_AT = NOW.plusSeconds(43_200);
    private static final Instant ROUND_DECISION_DEADLINE = NOW.plusSeconds(86_400);

    @Mock
    LoadProjectApplicationPort loadProjectApplicationPort;
    @Mock
    SaveProjectApplicationPort saveProjectApplicationPort;
    @Mock
    LoadProjectApplicationFormPort loadProjectApplicationFormPort;
    @Mock
    LoadProjectPartQuotaPort loadProjectPartQuotaPort;
    @Mock
    LoadProjectMemberPort loadProjectMemberPort;
    @Mock
    LoadProjectMatchingRoundPort loadProjectMatchingRoundPort;
    @Mock
    ManageFormResponseUseCase manageFormResponseUseCase;
    @Mock
    GetChallengerUseCase getChallengerUseCase;

    @InjectMocks
    ProjectApplicationCommandService sut;

    @Nested
    class decide {

        @Test
        void APPROVED_입력시_도메인_approve_호출_후_저장한다() {
            ProjectApplication application = applicationWithStatus(ProjectApplicationStatus.SUBMITTED);
            given(loadProjectApplicationPort.findById(APPLICATION_ID)).willReturn(Optional.of(application));

            ProjectApplicationInfo info = sut.decide(
                APPLICATION_ID, ApplicationDecisionStatus.APPROVED, "역량 우수", DECIDER_MEMBER_ID
            );

            assertThat(application.getStatus()).isEqualTo(ProjectApplicationStatus.APPROVED);
            assertThat(application.getStatusChangedMemberId()).isEqualTo(DECIDER_MEMBER_ID);
            assertThat(application.getStatusChangeReason()).isEqualTo("역량 우수");
            assertThat(info.applicationId()).isEqualTo(APPLICATION_ID);
            assertThat(info.status()).isEqualTo(ProjectApplicationStatus.APPROVED);
            then(saveProjectApplicationPort).should(times(1)).save(application);
        }

        @Test
        void REJECTED_입력시_도메인_reject_호출_후_저장한다() {
            ProjectApplication application = applicationWithStatus(ProjectApplicationStatus.SUBMITTED);
            given(loadProjectApplicationPort.findById(APPLICATION_ID)).willReturn(Optional.of(application));

            sut.decide(APPLICATION_ID, ApplicationDecisionStatus.REJECTED, "면접 결과", DECIDER_MEMBER_ID);

            assertThat(application.getStatus()).isEqualTo(ProjectApplicationStatus.REJECTED);
            assertThat(application.getStatusChangeReason()).isEqualTo("면접 결과");
            then(saveProjectApplicationPort).should(times(1)).save(application);
        }

        @Test
        void PENDING_입력시_도메인_revertToPending_호출하고_사유는_초기화된다() {
            ProjectApplication application = applicationWithStatus(ProjectApplicationStatus.APPROVED);
            ReflectionTestUtils.setField(application, "statusChangeReason", "이전 사유");
            given(loadProjectApplicationPort.findById(APPLICATION_ID)).willReturn(Optional.of(application));

            sut.decide(APPLICATION_ID, ApplicationDecisionStatus.PENDING, "무시되는 사유", DECIDER_MEMBER_ID);

            assertThat(application.getStatus()).isEqualTo(ProjectApplicationStatus.SUBMITTED);
            assertThat(application.getStatusChangeReason()).isNull();
            then(saveProjectApplicationPort).should(times(1)).save(application);
        }

        @Test
        void reason이_null이어도_정상_처리된다() {
            ProjectApplication application = applicationWithStatus(ProjectApplicationStatus.SUBMITTED);
            given(loadProjectApplicationPort.findById(APPLICATION_ID)).willReturn(Optional.of(application));

            sut.decide(APPLICATION_ID, ApplicationDecisionStatus.APPROVED, null, DECIDER_MEMBER_ID);

            assertThat(application.getStatusChangeReason()).isNull();
        }

        @Test
        void 존재하지_않는_지원서면_PROJECT_APPLICATION_NOT_FOUND() {
            given(loadProjectApplicationPort.findById(APPLICATION_ID)).willReturn(Optional.empty());

            assertThatThrownBy(() -> sut.decide(
                APPLICATION_ID, ApplicationDecisionStatus.APPROVED, null, DECIDER_MEMBER_ID
            ))
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.PROJECT_APPLICATION_NOT_FOUND);
            then(saveProjectApplicationPort).should(never()).save(any());
        }

        @Test
        void DRAFT_상태_지원서면_도메인_예외_전파_후_저장하지_않는다() {
            ProjectApplication application = applicationWithStatus(ProjectApplicationStatus.DRAFT);
            given(loadProjectApplicationPort.findById(APPLICATION_ID)).willReturn(Optional.of(application));

            assertThatThrownBy(() -> sut.decide(
                APPLICATION_ID, ApplicationDecisionStatus.APPROVED, null, DECIDER_MEMBER_ID
            ))
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.PROJECT_APPLICATION_DECISION_INVALID_TRANSITION);
            then(saveProjectApplicationPort).should(never()).save(any());
        }

        @Test
        void 차수_종료_후에는_도메인_예외_전파_후_저장하지_않는다() {
            ProjectMatchingRound expiredRound = openRound();
            ReflectionTestUtils.setField(expiredRound, "decisionDeadline", NOW.minusSeconds(60));
            ProjectApplication application = ProjectApplication.create(
                applicationForm(), 999L, APPLICANT_MEMBER_ID, expiredRound
            );
            ReflectionTestUtils.setField(application, "id", APPLICATION_ID);
            ReflectionTestUtils.setField(application, "status", ProjectApplicationStatus.SUBMITTED);
            given(loadProjectApplicationPort.findById(APPLICATION_ID)).willReturn(Optional.of(application));

            assertThatThrownBy(() -> sut.decide(
                APPLICATION_ID, ApplicationDecisionStatus.APPROVED, null, DECIDER_MEMBER_ID
            ))
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.PROJECT_MATCHING_ROUND_LOCKED);
            then(saveProjectApplicationPort).should(never()).save(any());
        }
    }

    private ProjectApplication applicationWithStatus(ProjectApplicationStatus status) {
        ProjectApplication application = ProjectApplication.create(
            applicationForm(), 999L, APPLICANT_MEMBER_ID, openRound()
        );
        ReflectionTestUtils.setField(application, "id", APPLICATION_ID);
        ReflectionTestUtils.setField(application, "status", status);
        return application;
    }

    private ProjectMatchingRound openRound() {
        return ProjectMatchingRound.create(
            "기획-디자인 1차 매칭", null,
            MatchingType.PLAN_DESIGN, MatchingPhase.FIRST, 1L,
            ROUND_STARTS_AT, ROUND_ENDS_AT, ROUND_DECISION_DEADLINE
        );
    }

    private ProjectApplicationForm applicationForm() {
        Project project = Project.createDraft(1L, 2L, 999L, 7L, 999L);
        return ProjectApplicationForm.create(project, 500L);
    }
}
