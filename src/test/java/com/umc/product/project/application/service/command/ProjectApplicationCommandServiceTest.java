package com.umc.product.project.application.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
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
import com.umc.product.project.domain.ProjectPartQuota;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.enums.ProjectApplicationStatus;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;
import com.umc.product.survey.application.port.in.command.ManageFormResponseUseCase;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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

    @BeforeEach
    void setUpQuotaMocks() {
        // 기본: quota 충분 (ChallengerPart.WEB, TO 6, active 0, 같은 차수 APPROVED 0)
        given(getChallengerUseCase.getByMemberIdAndGisuId(any(), any()))
            .willReturn(challengerWithPart(ChallengerPart.WEB));
        given(loadProjectPartQuotaPort.listByProjectId(any()))
            .willReturn(List.of(partQuotaForWeb(6L)));
        given(loadProjectMemberPort.countByProjectIdGroupByPart(any()))
            .willReturn(Map.of());
        given(loadProjectApplicationPort.listByMatchingRoundId(any()))
            .willReturn(List.of());
    }

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

        @Test
        void APPROVED_토글시_active_멤버가_TO를_채워_남은_자리가_없으면_QUOTA_EXCEEDED() {
            ProjectApplication application = applicationWithStatus(ProjectApplicationStatus.SUBMITTED);
            given(loadProjectApplicationPort.findById(APPLICATION_ID)).willReturn(Optional.of(application));
            given(loadProjectMemberPort.countByProjectIdGroupByPart(any()))
                .willReturn(Map.of(ChallengerPart.WEB, 6L));   // active 6 == TO 6 → 남은 자리 0

            assertThatThrownBy(() -> sut.decide(
                APPLICATION_ID, ApplicationDecisionStatus.APPROVED, null, DECIDER_MEMBER_ID
            ))
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.PROJECT_APPLICATION_QUOTA_EXCEEDED);
            then(saveProjectApplicationPort).should(never()).save(any());
        }

        @Test
        void APPROVED_토글시_active_멤버와_현재차수_APPROVED_합산이_TO를_초과하면_QUOTA_EXCEEDED() {
            ProjectApplication application = applicationWithStatus(ProjectApplicationStatus.SUBMITTED);
            given(loadProjectApplicationPort.findById(APPLICATION_ID)).willReturn(Optional.of(application));
            given(loadProjectMemberPort.countByProjectIdGroupByPart(any()))
                .willReturn(Map.of(ChallengerPart.WEB, 4L));   // active 4
            // 현재 차수 APPROVED 2명 (다른 application) → 4 + 2 + 1 = 7 > TO 6
            given(loadProjectApplicationPort.listByMatchingRoundId(any()))
                .willReturn(List.of(
                    otherApprovedApplication(701L, 71L, application.getApplicationForm()),
                    otherApprovedApplication(702L, 72L, application.getApplicationForm())
                ));
            given(getChallengerUseCase.batchGetByMemberIdsAndGisuId(any(), any()))
                .willReturn(Map.of(
                    71L, challengerWith(71L, ChallengerPart.WEB),
                    72L, challengerWith(72L, ChallengerPart.WEB)
                ));

            assertThatThrownBy(() -> sut.decide(
                APPLICATION_ID, ApplicationDecisionStatus.APPROVED, null, DECIDER_MEMBER_ID
            ))
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.PROJECT_APPLICATION_QUOTA_EXCEEDED);
        }

        @Test
        void 이미_APPROVED인_지원서를_다시_APPROVED로_토글하면_quota_검증_우회() {
            ProjectApplication application = applicationWithStatus(ProjectApplicationStatus.APPROVED);
            given(loadProjectApplicationPort.findById(APPLICATION_ID)).willReturn(Optional.of(application));
            // active 6 으로 quota 초과 상황이라도 — 이미 APPROVED 인 application 재토글은 검증 우회
            given(loadProjectMemberPort.countByProjectIdGroupByPart(any()))
                .willReturn(Map.of(ChallengerPart.WEB, 6L));

            sut.decide(APPLICATION_ID, ApplicationDecisionStatus.APPROVED, "재확인", DECIDER_MEMBER_ID);

            assertThat(application.getStatus()).isEqualTo(ProjectApplicationStatus.APPROVED);
            then(saveProjectApplicationPort).should(times(1)).save(application);
        }

        @Test
        void REJECTED_토글은_quota_검증을_적용하지_않는다() {
            ProjectApplication application = applicationWithStatus(ProjectApplicationStatus.APPROVED);
            given(loadProjectApplicationPort.findById(APPLICATION_ID)).willReturn(Optional.of(application));
            given(loadProjectMemberPort.countByProjectIdGroupByPart(any()))
                .willReturn(Map.of(ChallengerPart.WEB, 99L));   // 초과 상황이지만 REJECTED 는 무관

            sut.decide(APPLICATION_ID, ApplicationDecisionStatus.REJECTED, null, DECIDER_MEMBER_ID);

            assertThat(application.getStatus()).isEqualTo(ProjectApplicationStatus.REJECTED);
        }

        @Test
        void PENDING_토글은_quota_검증을_적용하지_않는다() {
            ProjectApplication application = applicationWithStatus(ProjectApplicationStatus.APPROVED);
            given(loadProjectApplicationPort.findById(APPLICATION_ID)).willReturn(Optional.of(application));
            given(loadProjectMemberPort.countByProjectIdGroupByPart(any()))
                .willReturn(Map.of(ChallengerPart.WEB, 99L));

            sut.decide(APPLICATION_ID, ApplicationDecisionStatus.PENDING, null, DECIDER_MEMBER_ID);

            assertThat(application.getStatus()).isEqualTo(ProjectApplicationStatus.SUBMITTED);
        }

        @Test
        void 다른_part로_APPROVED인_application은_현재_part_quota_검증에_영향을_주지_않는다() {
            ProjectApplication application = applicationWithStatus(ProjectApplicationStatus.SUBMITTED);
            ProjectApplication iosApp = otherApprovedApplication(701L, 71L, application.getApplicationForm());
            given(loadProjectApplicationPort.findById(APPLICATION_ID)).willReturn(Optional.of(application));
            given(loadProjectMemberPort.countByProjectIdGroupByPart(any()))
                .willReturn(Map.of(ChallengerPart.WEB, 5L));   // WEB active 5, TO 6 → 남은 1
            given(loadProjectApplicationPort.listByMatchingRoundId(any()))
                .willReturn(List.of(iosApp));
            given(getChallengerUseCase.getByMemberIdAndGisuId(eq(APPLICANT_MEMBER_ID), any()))
                .willReturn(challengerWith(APPLICANT_MEMBER_ID, ChallengerPart.WEB));
            given(getChallengerUseCase.batchGetByMemberIdsAndGisuId(any(), any()))
                .willReturn(Map.of(71L, challengerWith(71L, ChallengerPart.IOS)));

            sut.decide(APPLICATION_ID, ApplicationDecisionStatus.APPROVED, null, DECIDER_MEMBER_ID);

            assertThat(application.getStatus()).isEqualTo(ProjectApplicationStatus.APPROVED);
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
        ReflectionTestUtils.setField(project, "id", 100L);
        return ProjectApplicationForm.create(project, 500L);
    }

    private ChallengerInfo challengerWithPart(ChallengerPart part) {
        return ChallengerInfo.builder()
            .challengerId(1L).memberId(APPLICANT_MEMBER_ID).gisuId(1L)
            .part(part).challengerPoints(new ArrayList<>()).totalPoints(0.0)
            .build();
    }

    private ProjectPartQuota partQuotaForWeb(Long quota) {
        Project project = Project.createDraft(1L, 2L, 999L, 7L, 999L);
        return ProjectPartQuota.create(project, ChallengerPart.WEB, quota, 999L);
    }

    private ProjectApplication otherApprovedApplication(Long id, Long applicantMemberId, ProjectApplicationForm form) {
        ProjectApplication app = ProjectApplication.create(form, 999L, applicantMemberId, openRound());
        ReflectionTestUtils.setField(app, "id", id);
        ReflectionTestUtils.setField(app, "status", ProjectApplicationStatus.APPROVED);
        return app;
    }

    private ChallengerInfo challengerWith(Long memberId, ChallengerPart part) {
        return ChallengerInfo.builder()
            .challengerId(memberId * 10).memberId(memberId).gisuId(1L)
            .part(part).challengerPoints(new ArrayList<>()).totalPoints(0.0)
            .build();
    }
}
