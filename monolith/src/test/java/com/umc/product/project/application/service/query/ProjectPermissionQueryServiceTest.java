package com.umc.product.project.application.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;

import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.umc.product.authorization.application.port.in.CheckPermissionUseCase;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourcePermission;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.authorization.domain.RoleAttribute;
import com.umc.product.authorization.domain.SubjectAttributes;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.common.domain.enums.ChallengerStatus;
import com.umc.product.common.domain.enums.OrganizationType;
import com.umc.product.project.application.access.ProjectApplicationAccessScope;
import com.umc.product.project.application.access.ProjectApplicationAccessScopeResolver;
import com.umc.product.project.application.port.in.query.dto.ProjectPermissionCapabilityInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectPermissionInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectPermissionReason;
import com.umc.product.project.application.port.out.LoadProjectApplicationFormPort;
import com.umc.product.project.application.port.out.LoadProjectMatchingRoundPort;
import com.umc.product.project.application.port.out.LoadProjectMemberPort;
import com.umc.product.project.application.port.out.LoadProjectPartQuotaPort;
import com.umc.product.project.application.port.out.LoadProjectPort;
import com.umc.product.project.application.service.policy.ProjectManualMatchingAccessPolicy;
import com.umc.product.project.application.service.policy.ProjectMatchingAdjustmentPolicy;
import com.umc.product.project.application.service.policy.ProjectMatchingChapterResolver;
import com.umc.product.project.application.service.policy.ProjectQuotaEditPolicy;
import com.umc.product.project.application.service.policy.ProjectStatisticsAccessPolicy;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectApplicationForm;
import com.umc.product.project.domain.ProjectMatchingRound;
import com.umc.product.project.domain.ProjectPartQuota;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.enums.ProjectStatus;

@ExtendWith(MockitoExtension.class)
class ProjectPermissionQueryServiceTest {

    private static final Long REQUESTER_ID = 10L;
    private static final Long PROJECT_ID = 100L;
    private static final Long SECOND_PROJECT_ID = 101L;
    private static final Long GISU_ID = 1L;
    private static final Long CHAPTER_ID = 7L;

    @Mock
    CheckPermissionUseCase checkPermissionUseCase;
    @Mock
    LoadProjectPort loadProjectPort;
    @Mock
    LoadProjectApplicationFormPort loadProjectApplicationFormPort;
    @Mock
    LoadProjectPartQuotaPort loadProjectPartQuotaPort;
    @Mock
    LoadProjectMemberPort loadProjectMemberPort;
    @Mock
    LoadProjectMatchingRoundPort loadProjectMatchingRoundPort;
    @Mock
    GetChallengerUseCase getChallengerUseCase;
    @Mock
    ProjectApplicationAccessScopeResolver projectApplicationAccessScopeResolver;
    @Mock
    ProjectStatisticsAccessPolicy projectStatisticsAccessPolicy;

    @Mock
    ProjectMatchingChapterResolver projectMatchingChapterResolver;

    @Spy ProjectQuotaEditPolicy projectQuotaEditPolicy = new ProjectQuotaEditPolicy();
    @Mock Clock clock;

    @Mock
    ProjectManualMatchingAccessPolicy projectManualMatchingAccessPolicy;

    @Spy
    ProjectMatchingAdjustmentPolicy projectMatchingAdjustmentPolicy = new ProjectMatchingAdjustmentPolicy();

    @InjectMocks
    ProjectPermissionQueryService sut;

    @BeforeEach
    void setUpMatchingChapter() {
        lenient().when(clock.instant()).thenReturn(Instant.now());
        // 기본: 요청자의 매칭 지부는 테스트 프로젝트 지부와 같다
        lenient().when(projectMatchingChapterResolver.findMatchingChapterId(any(), any()))
            .thenReturn(Optional.of(CHAPTER_ID));
    }

    @Test
    void DRAFT_프로젝트는_작성자이고_이름과_지원폼이_있으면_검토_요청이_가능하다() {
        Project project = project(ProjectStatus.DRAFT, REQUESTER_ID, REQUESTER_ID, "서비스 리뉴얼");
        ProjectApplicationForm form = ProjectApplicationForm.create(project, 500L);
        SubjectAttributes subject = subject();
        givenBase(project, subject);
        givenForms(form);
        givenProjectPermission(subject, PermissionType.READ, true);
        givenProjectPermission(subject, PermissionType.EDIT, true);
        givenProjectPermission(subject, PermissionType.DELETE, true);
        givenProjectPermission(subject, PermissionType.MANAGE, false);
        given(projectApplicationAccessScopeResolver.resolveForProjectApplicantList(REQUESTER_ID, project))
            .willReturn(new ProjectApplicationAccessScope.ProjectScoped(PROJECT_ID));
        given(projectStatisticsAccessPolicy.canReadProjectStatistics(REQUESTER_ID, project)).willReturn(true);

        ProjectPermissionInfo result = sut.listByProjectIds(REQUESTER_ID, List.of(PROJECT_ID)).get(0);

        assertThat(result.status().canRequestReview().allowed()).isTrue();
        assertThat(result.applicationForm().canCreate().allowed()).isFalse();
        assertThat(result.applicationForm().canCreate().reasonCode())
            .isEqualTo(ProjectPermissionReason.NOT_IMPLEMENTED.name());
        assertThat(result.applicationForm().canCreate().reason())
            .isEqualTo("아직은 프로젝트에 여러 개의 폼을 연결하는 것을 허용하지 않아요.");
        assertThat(result.applicationForm().canPublish().reason())
            .isEqualTo("아직은 지원 폼 공개를 별도로 지원하지 않아요.");
        assertThat(result.applicationForm().canDelete().reason())
            .isEqualTo("아직은 지원 폼 삭제를 별도로 지원하지 않아요.");
        // MANAGE 권한이 없으면 상태와 무관하게 완료 불가 — 권한이 상태 검사보다 우선한다.
        assertThat(result.status().canComplete().allowed()).isFalse();
        assertThat(result.status().canComplete().reasonCode())
            .isEqualTo(ProjectPermissionReason.PERMISSION_DENIED.name());
    }

    @Test
    void 권한이_없으면_세부_상태보다_PERMISSION_DENIED가_우선한다() {
        Project project = project(ProjectStatus.DRAFT, 999L, 999L, null);
        SubjectAttributes subject = subject();
        givenBase(project, subject);
        givenForms();
        givenProjectPermission(subject, PermissionType.EDIT, false);
        givenProjectPermission(subject, PermissionType.READ, false);
        givenProjectPermission(subject, PermissionType.DELETE, false);
        givenProjectPermission(subject, PermissionType.MANAGE, false);
        given(projectApplicationAccessScopeResolver.resolveForProjectApplicantList(REQUESTER_ID, project))
            .willReturn(new ProjectApplicationAccessScope.None());
        given(projectStatisticsAccessPolicy.canReadProjectStatistics(REQUESTER_ID, project)).willReturn(false);

        ProjectPermissionInfo result = sut.listByProjectIds(REQUESTER_ID, List.of(PROJECT_ID)).get(0);

        assertThat(result.canEditInfo().allowed()).isFalse();
        assertThat(result.canEditInfo().reasonCode()).isEqualTo(ProjectPermissionReason.PERMISSION_DENIED.name());
        assertThat(result.status().canRequestReview().reasonCode())
            .isEqualTo(ProjectPermissionReason.PERMISSION_DENIED.name());
        assertThat(result.statistics().canRead().reason()).isEqualTo("통계를 조회할 권한이 없어요.");
    }

    @Test
    void PENDING_REVIEW_프로젝트는_운영진이고_지원폼과_정원이_있으면_공개가_가능하다() {
        Project project = project(ProjectStatus.PENDING_REVIEW, 999L, 999L, "서비스 리뉴얼");
        ProjectApplicationForm form = ProjectApplicationForm.create(project, 500L);
        SubjectAttributes subject = subject();
        givenBase(project, subject);
        givenForms(form);
        givenQuotas(project, ChallengerPart.WEB);
        givenProjectPermission(subject, PermissionType.READ, true);
        givenProjectPermission(subject, PermissionType.EDIT, false);
        givenProjectPermission(subject, PermissionType.DELETE, false);
        givenProjectPermission(subject, PermissionType.MANAGE, true);
        given(projectApplicationAccessScopeResolver.resolveForProjectApplicantList(REQUESTER_ID, project))
            .willReturn(new ProjectApplicationAccessScope.ProjectScoped(PROJECT_ID));
        given(projectStatisticsAccessPolicy.canReadProjectStatistics(REQUESTER_ID, project)).willReturn(true);

        ProjectPermissionInfo result = sut.listByProjectIds(REQUESTER_ID, List.of(PROJECT_ID)).get(0);

        assertThat(result.status().canPublish().allowed()).isTrue();
        assertThat(result.status().canAbort().allowed()).isFalse();
        assertThat(result.status().canAbort().reasonCode()).isEqualTo(ProjectPermissionReason.INVALID_PROJECT_STATUS.name());
        assertThat(result.status().canAbort().reason()).isEqualTo("현재 진행 중인 프로젝트만 중단 시킬 수 있어요.");
        // 운영진이어도 PENDING_REVIEW 는 완료 불가 — 완료는 IN_PROGRESS 에서만 열린다.
        assertThat(result.status().canComplete().allowed()).isFalse();
        assertThat(result.status().canComplete().reasonCode())
            .isEqualTo(ProjectPermissionReason.INVALID_PROJECT_STATUS.name());
        assertThat(result.status().canComplete().reason())
            .isEqualTo("현재 진행 중인 프로젝트만 완료 처리할 수 있어요.");
    }

    @Test
    void IN_PROGRESS_프로젝트는_운영진이면_중단_가능하고_활성_차수가_있으면_지원폼_수정은_불가하다() {
        Project project = project(ProjectStatus.IN_PROGRESS, 999L, 999L, "서비스 리뉴얼");
        ProjectApplicationForm form = ProjectApplicationForm.create(project, 500L);
        SubjectAttributes subject = subject();
        givenBase(project, subject);
        givenForms(form);
        givenProjectPermission(subject, PermissionType.READ, true);
        givenProjectPermission(subject, PermissionType.EDIT, true);
        givenProjectPermission(subject, PermissionType.DELETE, true);
        givenProjectPermission(subject, PermissionType.MANAGE, true);
        given(loadProjectMatchingRoundPort.listOpenAt(eq(CHAPTER_ID), any(Instant.class)))
            .willReturn(List.of(openRound()));
        given(projectApplicationAccessScopeResolver.resolveForProjectApplicantList(REQUESTER_ID, project))
            .willReturn(new ProjectApplicationAccessScope.ProjectScoped(PROJECT_ID));
        given(projectStatisticsAccessPolicy.canReadProjectStatistics(REQUESTER_ID, project)).willReturn(true);

        ProjectPermissionInfo result = sut.listByProjectIds(REQUESTER_ID, List.of(PROJECT_ID)).get(0);

        assertThat(result.status().canAbort().allowed()).isTrue();
        // IN_PROGRESS + 운영진(MANAGE) 이면 완료도 가능하다 — 여기서 열린다.
        assertThat(result.status().canComplete().allowed()).isTrue();
        assertThat(result.applicationForm().canEdit().allowed()).isFalse();
        assertThat(result.applicationForm().canEdit().reasonCode())
            .isEqualTo(ProjectPermissionReason.ACTIVE_MATCHING_ROUND_EXISTS.name());
        assertThat(result.canDelete().reasonCode()).isEqualTo(ProjectPermissionReason.APPLICATION_PERIOD_IN_PROGRESS.name());
        assertThat(result.canEditInfo().allowed()).isFalse();
        assertThat(result.canEditInfo().reasonCode())
            .isEqualTo(ProjectPermissionReason.PROJECT_UPDATE_DURING_APPLICATION_PERIOD.name());
        assertThat(result.canTransferOwnership().allowed()).isTrue();
    }

    @Test
    void 판정_가능_플래그는_같은_기수_총괄단이면_허용한다() {
        Project project = project(ProjectStatus.IN_PROGRESS, 999L, 999L, "서비스 리뉴얼");
        ProjectApplicationForm form = ProjectApplicationForm.create(project, 500L);
        SubjectAttributes subject = subjectWithRoles(List.of(centralCoreRoleInGisu(GISU_ID)));
        givenBase(project, subject);
        givenForms(form);
        givenProjectPermission(subject, PermissionType.READ, true);
        givenProjectPermission(subject, PermissionType.EDIT, false);
        givenProjectPermission(subject, PermissionType.DELETE, false);
        givenProjectPermission(subject, PermissionType.MANAGE, false);
        given(loadProjectMatchingRoundPort.listOpenAt(eq(CHAPTER_ID), any(Instant.class)))
            .willReturn(List.of());
        given(projectApplicationAccessScopeResolver.resolveForProjectApplicantList(REQUESTER_ID, project))
            .willReturn(new ProjectApplicationAccessScope.ProjectScoped(PROJECT_ID));
        given(projectStatisticsAccessPolicy.canReadProjectStatistics(REQUESTER_ID, project)).willReturn(true);

        ProjectPermissionInfo result = sut.listByProjectIds(REQUESTER_ID, List.of(PROJECT_ID)).get(0);

        assertThat(result.application().canDecide().allowed()).isTrue();
    }

    @Test
    void 판정_가능_플래그는_다른_기수_총괄단이거나_역할이_없으면_거부한다() {
        Project project = project(ProjectStatus.IN_PROGRESS, 999L, 999L, "서비스 리뉴얼");
        ProjectApplicationForm form = ProjectApplicationForm.create(project, 500L);
        SubjectAttributes subject = subjectWithRoles(List.of(centralCoreRoleInGisu(GISU_ID + 1)));
        givenBase(project, subject);
        givenForms(form);
        givenProjectPermission(subject, PermissionType.READ, true);
        givenProjectPermission(subject, PermissionType.EDIT, false);
        givenProjectPermission(subject, PermissionType.DELETE, false);
        givenProjectPermission(subject, PermissionType.MANAGE, false);
        given(loadProjectMatchingRoundPort.listOpenAt(eq(CHAPTER_ID), any(Instant.class)))
            .willReturn(List.of());
        given(projectApplicationAccessScopeResolver.resolveForProjectApplicantList(REQUESTER_ID, project))
            .willReturn(new ProjectApplicationAccessScope.None());
        given(projectStatisticsAccessPolicy.canReadProjectStatistics(REQUESTER_ID, project)).willReturn(false);

        ProjectPermissionInfo result = sut.listByProjectIds(REQUESTER_ID, List.of(PROJECT_ID)).get(0);

        assertThat(result.application().canDecide().allowed()).isFalse();
        assertThat(result.application().canDecide().reasonCode())
            .isEqualTo(ProjectPermissionReason.PERMISSION_DENIED.name());
    }

    @Test
    void 지원_생성은_모집중이고_폼_정원_오픈차수_챌린저_조건이_모두_맞으면_가능하다() {
        Project project = project(ProjectStatus.IN_PROGRESS, 999L, 999L, "서비스 리뉴얼");
        ProjectApplicationForm form = ProjectApplicationForm.create(project, 500L);
        SubjectAttributes subject = subject();
        givenBase(project, subject);
        givenForms(form);
        givenQuotas(project, ChallengerPart.WEB);
        givenProjectPermission(subject, PermissionType.READ, true);
        givenProjectPermission(subject, PermissionType.EDIT, false);
        givenProjectPermission(subject, PermissionType.DELETE, false);
        givenProjectPermission(subject, PermissionType.MANAGE, false);
        givenApplicationPermission(subject, true);
        given(getChallengerUseCase.findByMemberIdAndGisuId(REQUESTER_ID, GISU_ID))
            .willReturn(Optional.of(challenger(ChallengerPart.WEB)));
        given(loadProjectMemberPort.existsByGisuAndMember(GISU_ID, REQUESTER_ID)).willReturn(false);
        given(loadProjectMatchingRoundPort.listOpenAt(eq(CHAPTER_ID), any(Instant.class)))
            .willReturn(List.of(openRound()));
        given(projectApplicationAccessScopeResolver.resolveForProjectApplicantList(REQUESTER_ID, project))
            .willReturn(new ProjectApplicationAccessScope.None());
        given(projectStatisticsAccessPolicy.canReadProjectStatistics(REQUESTER_ID, project)).willReturn(false);

        ProjectPermissionInfo result = sut.listByProjectIds(REQUESTER_ID, List.of(PROJECT_ID)).get(0);

        assertThat(result.application().canCreate().allowed()).isTrue();
    }

    @Test
    void 지원_생성은_매칭_지부가_프로젝트_지부와_다르면_CHAPTER_MISMATCH로_거부된다() {
        Project project = project(ProjectStatus.IN_PROGRESS, 999L, 999L, "서비스 리뉴얼");
        ProjectApplicationForm form = ProjectApplicationForm.create(project, 500L);
        SubjectAttributes subject = subject();
        givenBase(project, subject);
        givenForms(form);
        givenQuotas(project, ChallengerPart.DESIGN);
        givenApplicationCreatePermissions(subject, PROJECT_ID);
        given(getChallengerUseCase.findByMemberIdAndGisuId(REQUESTER_ID, GISU_ID))
            .willReturn(Optional.of(challenger(ChallengerPart.DESIGN)));
        given(projectMatchingChapterResolver.findMatchingChapterId(REQUESTER_ID, GISU_ID))
            .willReturn(Optional.of(CHAPTER_ID + 1));
        given(projectApplicationAccessScopeResolver.resolveForProjectApplicantList(REQUESTER_ID, project))
            .willReturn(new ProjectApplicationAccessScope.None());
        given(projectStatisticsAccessPolicy.canReadProjectStatistics(REQUESTER_ID, project)).willReturn(false);

        ProjectPermissionInfo result = sut.listByProjectIds(REQUESTER_ID, List.of(PROJECT_ID)).get(0);

        assertThat(result.application().canCreate().allowed()).isFalse();
        assertThat(result.application().canCreate().reasonCode()).isEqualTo(ProjectPermissionReason.CHAPTER_MISMATCH.name());
    }

    @Test
    void 같은_지부와_기수의_프로젝트는_권한_계산용_조회_결과를_캐시한다() {
        Project firstProject = project(PROJECT_ID, GISU_ID, CHAPTER_ID, ProjectStatus.IN_PROGRESS, 999L, 999L, "서비스 리뉴얼");
        Project secondProject = project(
            SECOND_PROJECT_ID,
            GISU_ID,
            CHAPTER_ID,
            ProjectStatus.IN_PROGRESS,
            998L,
            998L,
            "운영 도구"
        );
        ProjectApplicationForm firstForm = ProjectApplicationForm.create(firstProject, 500L);
        ProjectApplicationForm secondForm = ProjectApplicationForm.create(secondProject, 501L);
        SubjectAttributes subject = subject();
        given(checkPermissionUseCase.loadSubject(REQUESTER_ID)).willReturn(subject);
        given(loadProjectPort.listByIds(List.of(PROJECT_ID, SECOND_PROJECT_ID)))
            .willReturn(List.of(firstProject, secondProject));
        given(loadProjectApplicationFormPort.findAllByProjectIds(anyCollection()))
            .willReturn(Map.of(PROJECT_ID, firstForm, SECOND_PROJECT_ID, secondForm));
        given(loadProjectPartQuotaPort.listByProjectIdsGroupedByProjectId(anyCollection()))
            .willReturn(Map.of(
                PROJECT_ID,
                List.of(ProjectPartQuota.create(firstProject, ChallengerPart.WEB, 1L, REQUESTER_ID)),
                SECOND_PROJECT_ID,
                List.of(ProjectPartQuota.create(secondProject, ChallengerPart.WEB, 1L, REQUESTER_ID))
            ));
        givenApplicationCreatePermissions(subject, PROJECT_ID);
        givenApplicationCreatePermissions(subject, SECOND_PROJECT_ID);
        given(getChallengerUseCase.findByMemberIdAndGisuId(REQUESTER_ID, GISU_ID))
            .willReturn(Optional.of(challenger(ChallengerPart.WEB)));
        given(loadProjectMemberPort.existsByGisuAndMember(GISU_ID, REQUESTER_ID)).willReturn(false);
        given(loadProjectMatchingRoundPort.listOpenAt(eq(CHAPTER_ID), any(Instant.class)))
            .willReturn(List.of(openRound()));
        given(projectApplicationAccessScopeResolver.resolveForProjectApplicantList(REQUESTER_ID, firstProject))
            .willReturn(new ProjectApplicationAccessScope.None());
        given(projectApplicationAccessScopeResolver.resolveForProjectApplicantList(REQUESTER_ID, secondProject))
            .willReturn(new ProjectApplicationAccessScope.None());
        given(projectStatisticsAccessPolicy.canReadProjectStatistics(REQUESTER_ID, firstProject)).willReturn(false);
        given(projectStatisticsAccessPolicy.canReadProjectStatistics(REQUESTER_ID, secondProject)).willReturn(false);

        List<ProjectPermissionInfo> results = sut.listByProjectIds(
            REQUESTER_ID,
            List.of(PROJECT_ID, SECOND_PROJECT_ID)
        );

        assertThat(results).hasSize(2);
        assertThat(results)
            .extracting(result -> result.application().canCreate().allowed())
            .containsExactly(true, true);
        then(loadProjectMatchingRoundPort).should(times(1)).listOpenAt(eq(CHAPTER_ID), any(Instant.class));
        then(getChallengerUseCase).should(times(1)).findByMemberIdAndGisuId(REQUESTER_ID, GISU_ID);
        then(loadProjectMemberPort).should(times(1)).existsByGisuAndMember(GISU_ID, REQUESTER_ID);
        then(projectManualMatchingAccessPolicy).should(times(1)).canManage(REQUESTER_ID, GISU_ID, CHAPTER_ID);
    }

    @Test
    void 프로젝트가_없으면_exists_false와_PROJECT_NOT_FOUND를_반환한다() {
        SubjectAttributes subject = subject();
        given(checkPermissionUseCase.loadSubject(REQUESTER_ID)).willReturn(subject);
        given(loadProjectPort.listByIds(List.of(PROJECT_ID))).willReturn(List.of());

        ProjectPermissionInfo result = sut.listByProjectIds(REQUESTER_ID, List.of(PROJECT_ID)).get(0);

        assertThat(result.exists()).isFalse();
        assertThat(result.canEditInfo().reasonCode()).isEqualTo(ProjectPermissionReason.PROJECT_NOT_FOUND.name());
        assertThat(result.application().canCreate().reasonCode())
            .isEqualTo(ProjectPermissionReason.PROJECT_NOT_FOUND.name());
    }

    @Test
    void 지원_생성_권한은_모집_종료_시_팀_소속자에게도_차수_미오픈_사유를_반환한다() {
        givenApplicationCreateConditions(List.of());
        lenient().when(loadProjectMemberPort.existsByGisuAndMember(GISU_ID, REQUESTER_ID)).thenReturn(true);

        ProjectPermissionInfo result = sut.listByProjectIds(REQUESTER_ID, List.of(PROJECT_ID)).get(0);

        assertThat(result.application().canCreate())
            .isEqualTo(ProjectPermissionCapabilityInfo.denied(ProjectPermissionReason.MATCHING_ROUND_NOT_OPEN));
    }

    @Test
    void 지원_생성_권한은_모집_중_팀_소속자에게_이미_팀에_소속됨_사유를_반환한다() {
        givenApplicationCreateConditions(List.of(openRound()));
        given(loadProjectMemberPort.existsByGisuAndMember(GISU_ID, REQUESTER_ID)).willReturn(true);

        ProjectPermissionInfo result = sut.listByProjectIds(REQUESTER_ID, List.of(PROJECT_ID)).get(0);

        assertThat(result.application().canCreate()).isEqualTo(ProjectPermissionCapabilityInfo.denied(
            ProjectPermissionReason.PROJECT_APPLICATION_MEMBER_ALREADY_IN_TEAM
        ));
    }

    @ParameterizedTest
    @CsvSource({
        "FIRST, false, false",
        "FIRST, true, true",
        "RANDOM_MATCHING, false, false",
        "RANDOM_MATCHING, true, false"
    })
    void 수동_배정과_해제_권한은_변경_가능_기간을_반영한다(
        MatchingPhase phase, boolean published, boolean allowed
    ) {
        // given
        Project project = project(ProjectStatus.IN_PROGRESS, 999L, 999L, "서비스 리뉴얼");
        SubjectAttributes subject = subjectWithRoles(List.of(centralCoreRoleInGisu(GISU_ID)));
        givenBase(project, subject);
        given(projectManualMatchingAccessPolicy.canManage(REQUESTER_ID, GISU_ID, CHAPTER_ID)).willReturn(true);
        Instant now = Instant.parse("2026-11-19T22:00:00+09:00");
        given(clock.instant()).willReturn(now);
        Instant decisionDeadline = now.minusSeconds(3600);
        Instant endsAt = phase == MatchingPhase.RANDOM_MATCHING ? decisionDeadline : now.minusSeconds(7200);
        ProjectMatchingRound round = ProjectMatchingRound.create("개발자 매칭", null, MatchingType.PLAN_DEVELOPER,
            phase, CHAPTER_ID, now.minusSeconds(10800), endsAt, decisionDeadline, now.minusSeconds(60));
        round.executeAutoDecision(null);
        if (published) {
            round.publishResult(now.minusSeconds(60));
        }
        ProjectMatchingRound second = ProjectMatchingRound.create("개발자 2차", null, MatchingType.PLAN_DEVELOPER,
            MatchingPhase.SECOND, CHAPTER_ID, now.plusSeconds(3600), now.plusSeconds(7200),
            now.plusSeconds(10800), now.plusSeconds(14400));
        List<ProjectMatchingRound> rounds = phase == MatchingPhase.FIRST ? List.of(round, second) : List.of(round);
        given(loadProjectMatchingRoundPort.listByChapterId(CHAPTER_ID)).willReturn(rounds);

        // when
        ProjectPermissionInfo result = sut.listByProjectIds(REQUESTER_ID, List.of(PROJECT_ID)).getFirst();

        // then
        ProjectPermissionCapabilityInfo expected = allowed ? ProjectPermissionCapabilityInfo.allow()
            : ProjectPermissionCapabilityInfo.denied(ProjectPermissionReason.PROJECT_MANUAL_MATCHING_NOT_ALLOWED_IN_PERIOD);
        assertThat(result.member().canAssign()).isEqualTo(expected);
        assertThat(result.member().canUnassign()).isEqualTo(expected);
        then(loadProjectMatchingRoundPort).should(times(1)).listByChapterId(CHAPTER_ID);
    }

    private void givenApplicationCreateConditions(List<ProjectMatchingRound> openRounds) {
        Project project = project(ProjectStatus.IN_PROGRESS, 999L, 999L, "서비스 리뉴얼");
        SubjectAttributes subject = subject();
        givenBase(project, subject);
        givenForms(ProjectApplicationForm.create(project, 500L));
        givenQuotas(project, ChallengerPart.WEB);
        givenApplicationCreatePermissions(subject, PROJECT_ID);
        given(getChallengerUseCase.findByMemberIdAndGisuId(REQUESTER_ID, GISU_ID))
            .willReturn(Optional.of(challenger(ChallengerPart.WEB)));
        given(loadProjectMatchingRoundPort.listOpenAt(eq(CHAPTER_ID), any(Instant.class)))
            .willReturn(openRounds);
        given(projectApplicationAccessScopeResolver.resolveForProjectApplicantList(REQUESTER_ID, project))
            .willReturn(new ProjectApplicationAccessScope.None());
    }

    private void givenBase(Project project, SubjectAttributes subject) {
        given(checkPermissionUseCase.loadSubject(REQUESTER_ID)).willReturn(subject);
        given(loadProjectPort.listByIds(List.of(PROJECT_ID))).willReturn(List.of(project));
        lenient().when(loadProjectPartQuotaPort.listByProjectIdsGroupedByProjectId(anyCollection()))
            .thenReturn(Map.of());
        lenient().when(loadProjectMatchingRoundPort.listOpenAt(eq(CHAPTER_ID), any(Instant.class)))
            .thenReturn(List.of());
    }

    private void givenForms(ProjectApplicationForm... forms) {
        Map<Long, ProjectApplicationForm> byProjectId = forms.length == 0
            ? Map.of()
            : Map.of(PROJECT_ID, forms[0]);
        given(loadProjectApplicationFormPort.findAllByProjectIds(anyCollection())).willReturn(byProjectId);
    }

    private void givenQuotas(Project project, ChallengerPart part) {
        given(loadProjectPartQuotaPort.listByProjectIdsGroupedByProjectId(anyCollection()))
            .willReturn(Map.of(PROJECT_ID, List.of(ProjectPartQuota.create(project, part, 1L, REQUESTER_ID))));
    }

    private void givenProjectPermission(SubjectAttributes subject, PermissionType permissionType, boolean allowed) {
        givenProjectPermission(subject, PROJECT_ID, permissionType, allowed);
    }

    private void givenProjectPermission(
        SubjectAttributes subject,
        Long projectId,
        PermissionType permissionType,
        boolean allowed
    ) {
        given(checkPermissionUseCase.check(
            eq(subject),
            eq(ResourcePermission.of(ResourceType.PROJECT, projectId, permissionType))
        )).willReturn(allowed);
    }

    private void givenApplicationPermission(SubjectAttributes subject, boolean allowed) {
        givenApplicationPermission(subject, PROJECT_ID, allowed);
    }

    private void givenApplicationPermission(SubjectAttributes subject, Long projectId, boolean allowed) {
        given(checkPermissionUseCase.check(
            eq(subject),
            eq(ResourcePermission.of(ResourceType.PROJECT_APPLICATION, projectId, PermissionType.WRITE))
        )).willReturn(allowed);
    }

    private void givenApplicationCreatePermissions(SubjectAttributes subject, Long projectId) {
        givenProjectPermission(subject, projectId, PermissionType.READ, true);
        givenProjectPermission(subject, projectId, PermissionType.EDIT, false);
        givenProjectPermission(subject, projectId, PermissionType.DELETE, false);
        givenProjectPermission(subject, projectId, PermissionType.MANAGE, false);
        givenApplicationPermission(subject, projectId, true);
    }

    private SubjectAttributes subject() {
        return SubjectAttributes.builder()
            .memberId(REQUESTER_ID)
            .schoolId(3L)
            .gisuChallengerInfos(List.of())
            .roleAttributes(List.of())
            .build();
    }

    private RoleAttribute centralCoreRoleInGisu(Long gisuId) {
        return new RoleAttribute(
            ChallengerRoleType.CENTRAL_PRESIDENT,
            OrganizationType.CENTRAL,
            null, null, gisuId
        );
    }

    private SubjectAttributes subjectWithRoles(List<RoleAttribute> roles) {
        return SubjectAttributes.builder()
            .memberId(REQUESTER_ID)
            .schoolId(3L)
            .gisuChallengerInfos(List.of())
            .roleAttributes(roles)
            .build();
    }

    private Project project(ProjectStatus status, Long ownerId, Long creatorId, String name) {
        return project(PROJECT_ID, GISU_ID, CHAPTER_ID, status, ownerId, creatorId, name);
    }

    private Project project(
        Long projectId,
        Long gisuId,
        Long chapterId,
        ProjectStatus status,
        Long ownerId,
        Long creatorId,
        String name
    ) {
        Project project = Project.createDraft(gisuId, chapterId, ownerId, 3L, creatorId);
        ReflectionTestUtils.setField(project, "id", projectId);
        ReflectionTestUtils.setField(project, "status", status);
        ReflectionTestUtils.setField(project, "name", name);
        return project;
    }

    private ChallengerInfo challenger(ChallengerPart part) {
        return ChallengerInfo.builder()
            .memberId(REQUESTER_ID)
            .gisuId(GISU_ID)
            .part(part)
            .challengerStatus(ChallengerStatus.ACTIVE)
            .build();
    }

    private ProjectMatchingRound openRound() {
        return ProjectMatchingRound.create(
            "1차 매칭",
            null,
            MatchingType.PLAN_DEVELOPER,
            MatchingPhase.FIRST,
            CHAPTER_ID,
            Instant.now().minusSeconds(60),
            Instant.now().plusSeconds(60),
            Instant.now().plusSeconds(120),
            Instant.now().plusSeconds(7320)
        );
    }

    @SuppressWarnings("unchecked")
    private Collection<Long> anyCollection() {
        return any(Collection.class);
    }
}
