package com.umc.product.project.application.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.umc.product.authorization.application.port.in.CheckPermissionUseCase;
import com.umc.product.authorization.domain.RoleAttribute;
import com.umc.product.authorization.domain.SubjectAttributes;
import com.umc.product.authorization.domain.SystemRoleType;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.form.application.port.in.query.GetFormResponseUseCase;
import com.umc.product.form.application.port.in.query.GetFormUseCase;
import com.umc.product.form.application.port.in.query.dto.AnswerInfo;
import com.umc.product.form.application.port.in.query.dto.FormResponseWithAnswersInfo;
import com.umc.product.form.application.port.in.query.dto.FormWithStructureInfo;
import com.umc.product.form.domain.enums.FormResponseStatus;
import com.umc.product.form.domain.enums.QuestionType;
import com.umc.product.project.application.access.ProjectApplicationAccessScope;
import com.umc.product.project.application.access.ProjectApplicationAccessScopeResolver;
import com.umc.product.project.application.port.in.query.dto.GetMyProjectApplicationsQuery;
import com.umc.product.project.application.port.in.query.dto.GetProjectApplicationDetailQuery;
import com.umc.product.project.application.port.in.query.dto.ProjectApplicationDetailInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectApplicationSummaryInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectApplicationViewStatus;
import com.umc.product.project.application.port.in.query.dto.SearchProjectApplicationsBatchQuery;
import com.umc.product.project.application.port.in.query.dto.SearchProjectApplicationsQuery;
import com.umc.product.project.application.port.out.LoadProjectApplicationFormPolicyPort;
import com.umc.product.project.application.port.out.LoadProjectApplicationPort;
import com.umc.product.project.application.port.out.LoadProjectPort;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectApplication;
import com.umc.product.project.domain.ProjectApplicationForm;
import com.umc.product.project.domain.ProjectApplicationFormPolicy;
import com.umc.product.project.domain.ProjectMatchingRound;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.enums.ProjectApplicationStatus;
import com.umc.product.project.domain.enums.ProjectStatus;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;
import com.umc.product.storage.application.port.in.query.GetFileUseCase;
import com.umc.product.storage.application.port.in.query.dto.FileInfo;
import com.umc.product.storage.domain.enums.FileCategory;

@ExtendWith(MockitoExtension.class)
class ProjectApplicationQueryServiceTest {

    private static final Long REQUESTER_ID = 100L;
    private static final Long GISU_ID = 10L;
    private static final Instant LIST_NOW = Instant.parse("2026-11-07T09:00:00Z");
    @Mock
    Clock clock;
    @Mock
    CheckPermissionUseCase checkPermissionUseCase;
    @Mock
    LoadProjectApplicationPort loadProjectApplicationPort;
    @Mock
    LoadProjectPort loadProjectPort;
    @Mock
    LoadProjectApplicationFormPolicyPort loadProjectApplicationFormPolicyPort;
    @Mock
    GetChallengerUseCase getChallengerUseCase;
    @Mock
    GetFileUseCase getFileUseCase;
    @Mock
    GetFormUseCase getFormUseCase;
    @Mock
    GetFormResponseUseCase getFormResponseUseCase;
    @Mock
    ProjectApplicationAccessScopeResolver accessScopeResolver;
    @InjectMocks
    ProjectApplicationQueryService sut;

    @BeforeEach
    void 목록_조회_기준_시각을_설정한다() {
        lenient().when(clock.instant()).thenReturn(Instant.now());
    }

    private static <T> T newInstance(Class<T> clazz) {
        try {
            var constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ============================================================
    //          listMyApplications (본인 지원 내역 자원 조회) 테스트
    //   - 자원(ProjectApplication) 한 종류만 반환한다.
    //   - 화면 카드 합성 / 랜덤 매칭 멤버 합성은 Web Assembler 책임.
    // ============================================================

    @Test
    @DisplayName("listMyApplications_해당_기수_챌린저가_아니면_빈_리스트_반환")
    void 챌린저_아님_빈_리스트() {
        // given
        GetMyProjectApplicationsQuery query = queryOf(null);
        given(getChallengerUseCase.findByMemberIdAndGisuId(REQUESTER_ID, GISU_ID))
            .willReturn(Optional.empty());

        // when
        List<ProjectApplicationSummaryInfo> result = sut.listMyApplications(query);

        // then
        assertThat(result).isEmpty();
        verify(loadProjectApplicationPort, never())
            .searchMyApplications(any(), any(), any(), any());
    }

    @Test
    @DisplayName("listMyApplications_PLAN_파트는_지원_대상이_아니므로_빈_리스트")
    void PLAN_파트_빈_리스트() {
        // given
        GetMyProjectApplicationsQuery query = queryOf(null);
        given(getChallengerUseCase.findByMemberIdAndGisuId(REQUESTER_ID, GISU_ID))
            .willReturn(Optional.of(challengerOf(ChallengerPart.PLAN)));

        // when
        List<ProjectApplicationSummaryInfo> result = sut.listMyApplications(query);

        // then
        assertThat(result).isEmpty();
        verify(loadProjectApplicationPort, never())
            .searchMyApplications(any(), any(), any(), any());
    }

    @Test
    @DisplayName("listMyApplications_ADMIN_파트는_지원_대상이_아니므로_빈_리스트")
    void ADMIN_파트_빈_리스트() {
        // given
        GetMyProjectApplicationsQuery query = queryOf(null);
        given(getChallengerUseCase.findByMemberIdAndGisuId(REQUESTER_ID, GISU_ID))
            .willReturn(Optional.of(challengerOf(ChallengerPart.ADMIN)));

        // when
        List<ProjectApplicationSummaryInfo> result = sut.listMyApplications(query);

        // then
        assertThat(result).isEmpty();
        verify(loadProjectApplicationPort, never())
            .searchMyApplications(any(), any(), any(), any());
    }

    @Test
    @DisplayName("listMyApplications_DESIGN_파트는_PLAN_DESIGN_매칭으로_필터링")
    void DESIGN_PLAN_DESIGN_매칭() {
        // given
        GetMyProjectApplicationsQuery query = queryOf(null);
        given(getChallengerUseCase.findByMemberIdAndGisuId(REQUESTER_ID, GISU_ID))
            .willReturn(Optional.of(challengerOf(ChallengerPart.DESIGN)));
        given(loadProjectApplicationPort.searchMyApplications(
            REQUESTER_ID, GISU_ID, MatchingType.PLAN_DESIGN, null))
            .willReturn(List.of());

        // when
        sut.listMyApplications(query);

        // then
        verify(loadProjectApplicationPort)
            .searchMyApplications(REQUESTER_ID, GISU_ID, MatchingType.PLAN_DESIGN, null);
    }

    @Test
    @DisplayName("listMyApplications_WEB_파트는_PLAN_DEVELOPER_매칭으로_필터링")
    void WEB_PLAN_DEVELOPER_매칭() {
        // given
        GetMyProjectApplicationsQuery query = queryOf(null);
        given(getChallengerUseCase.findByMemberIdAndGisuId(REQUESTER_ID, GISU_ID))
            .willReturn(Optional.of(challengerOf(ChallengerPart.WEB)));
        given(loadProjectApplicationPort.searchMyApplications(
            REQUESTER_ID, GISU_ID, MatchingType.PLAN_DEVELOPER, null))
            .willReturn(List.of());

        // when
        sut.listMyApplications(query);

        // then
        verify(loadProjectApplicationPort)
            .searchMyApplications(REQUESTER_ID, GISU_ID, MatchingType.PLAN_DEVELOPER, null);
    }

    @Test
    @DisplayName("listMyApplications_status_명시시_해당_상태로_Port_호출")
    void status_명시_전달() {
        // given
        GetMyProjectApplicationsQuery query = queryOf(ProjectApplicationStatus.SUBMITTED);
        given(getChallengerUseCase.findByMemberIdAndGisuId(REQUESTER_ID, GISU_ID))
            .willReturn(Optional.of(challengerOf(ChallengerPart.WEB)));
        given(loadProjectApplicationPort.searchMyApplications(
            REQUESTER_ID, GISU_ID, MatchingType.PLAN_DEVELOPER, ProjectApplicationStatus.SUBMITTED))
            .willReturn(List.of());

        // when
        sut.listMyApplications(query);

        // then
        verify(loadProjectApplicationPort).searchMyApplications(
            REQUESTER_ID, GISU_ID, MatchingType.PLAN_DEVELOPER, ProjectApplicationStatus.SUBMITTED);
    }

    @Test
    @DisplayName("listMyApplications_status_필터가_있어도_decisionDeadline_전이면_결과에서_제외")
    void filteredStatusApplicationBeforeDecisionDeadlineIsExcluded() {
        // given
        Project project = createProject(1L, "프로젝트A", "thumb-1", 99L);
        ProjectMatchingRound round = createMatchingRound(
            7L, MatchingType.PLAN_DEVELOPER, MatchingPhase.FIRST);
        ProjectApplication application = createApplication(
            55L, project, round, ProjectApplicationStatus.APPROVED);

        GetMyProjectApplicationsQuery query = queryOf(ProjectApplicationStatus.APPROVED);
        given(getChallengerUseCase.findByMemberIdAndGisuId(REQUESTER_ID, GISU_ID))
            .willReturn(Optional.of(challengerOf(ChallengerPart.WEB)));
        given(loadProjectApplicationPort.searchMyApplications(
            eq(REQUESTER_ID), eq(GISU_ID), eq(MatchingType.PLAN_DEVELOPER),
            eq(ProjectApplicationStatus.APPROVED)))
            .willReturn(List.of(application));

        // when
        List<ProjectApplicationSummaryInfo> result = sut.listMyApplications(query);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("listMyApplications_status_필터가_있고_decisionDeadline_후면_결과에_포함")
    void filteredStatusApplicationAfterDecisionDeadlineIsIncluded() {
        // given
        Project project = createProject(1L, "프로젝트A", "thumb-1", 99L);
        ProjectMatchingRound round = createMatchingRound(
            7L, MatchingType.PLAN_DEVELOPER, MatchingPhase.FIRST);
        markDecisionDeadlinePassed(round);
        ProjectApplication application = createApplication(
            55L, project, round, ProjectApplicationStatus.APPROVED);

        GetMyProjectApplicationsQuery query = queryOf(ProjectApplicationStatus.APPROVED);
        given(getChallengerUseCase.findByMemberIdAndGisuId(REQUESTER_ID, GISU_ID))
            .willReturn(Optional.of(challengerOf(ChallengerPart.WEB)));
        given(loadProjectApplicationPort.searchMyApplications(
            eq(REQUESTER_ID), eq(GISU_ID), eq(MatchingType.PLAN_DEVELOPER),
            eq(ProjectApplicationStatus.APPROVED)))
            .willReturn(List.of(application));

        // when
        List<ProjectApplicationSummaryInfo> result = sut.listMyApplications(query);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).status()).isEqualTo(ProjectApplicationStatus.APPROVED);
    }

    @Test
    @DisplayName("listMyApplications_이전_차수는_공개하고_현재_차수는_발표_전까지_상태를_가린다")
    void 이전_차수_결과는_유지하고_미공개_차수_상태는_가린다() {
        // given
        Project project = createProject(1L, "프로젝트A", "thumb-1", 99L);
        ProjectMatchingRound firstRound = createMatchingRound(
            7L, MatchingType.PLAN_DEVELOPER, MatchingPhase.FIRST);
        markDecisionDeadlinePassed(firstRound);
        ReflectionTestUtils.setField(firstRound, "resultPublishAt", java.time.Instant.now().minusSeconds(600));
        firstRound.executeAutoDecision(null);
        firstRound.publishResult(java.time.Instant.now());
        ProjectMatchingRound secondRound = createMatchingRound(
            8L, MatchingType.PLAN_DEVELOPER, MatchingPhase.SECOND);
        markDecisionDeadlinePassed(secondRound);
        ReflectionTestUtils.setField(secondRound, "resultPublishAt", java.time.Instant.now().plusSeconds(600));
        secondRound.executeAutoDecision(null);
        ProjectApplication firstApplication = createApplication(
            55L, project, firstRound, ProjectApplicationStatus.APPROVED);
        ProjectApplication secondApplication = createApplication(
            56L, project, secondRound, ProjectApplicationStatus.REJECTED);
        given(getChallengerUseCase.findByMemberIdAndGisuId(REQUESTER_ID, GISU_ID))
            .willReturn(Optional.of(challengerOf(ChallengerPart.WEB)));
        given(loadProjectApplicationPort.searchMyApplications(
            eq(REQUESTER_ID), eq(GISU_ID), eq(MatchingType.PLAN_DEVELOPER), eq(null)))
            .willReturn(List.of(firstApplication, secondApplication));

        // when
        List<ProjectApplicationSummaryInfo> result = sut.listMyApplications(queryOf(null));

        // then
        assertThat(result).extracting(ProjectApplicationSummaryInfo::status)
            .containsExactly(ProjectApplicationStatus.APPROVED, null);
    }

    @Test
    @DisplayName("listMyApplications_발표_시각이_지나도_발표_작업_완료_전이면_상태_필터_결과에서_제외한다")
    void 발표_작업_완료_전에는_상태_필터로_결과를_조회할_수_없다() {
        // given
        Project project = createProject(1L, "프로젝트A", "thumb-1", 99L);
        ProjectMatchingRound round = createMatchingRound(
            7L, MatchingType.PLAN_DEVELOPER, MatchingPhase.FIRST);
        markDecisionDeadlinePassed(round);
        ReflectionTestUtils.setField(round, "resultPublishAt", java.time.Instant.now().minusSeconds(600));
        round.executeAutoDecision(null);
        ProjectApplication application = createApplication(
            55L, project, round, ProjectApplicationStatus.APPROVED);
        given(getChallengerUseCase.findByMemberIdAndGisuId(REQUESTER_ID, GISU_ID))
            .willReturn(Optional.of(challengerOf(ChallengerPart.WEB)));
        given(loadProjectApplicationPort.searchMyApplications(
            eq(REQUESTER_ID), eq(GISU_ID), eq(MatchingType.PLAN_DEVELOPER), eq(ProjectApplicationStatus.APPROVED)))
            .willReturn(List.of(application));

        // when
        List<ProjectApplicationSummaryInfo> result = sut.listMyApplications(queryOf(ProjectApplicationStatus.APPROVED));

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("listMyApplications_지원_내역이_없으면_빈_리스트")
    void 지원_내역_없음_빈_리스트() {
        // given
        GetMyProjectApplicationsQuery query = queryOf(null);
        given(getChallengerUseCase.findByMemberIdAndGisuId(REQUESTER_ID, GISU_ID))
            .willReturn(Optional.of(challengerOf(ChallengerPart.WEB)));
        given(loadProjectApplicationPort.searchMyApplications(any(), any(), any(), any()))
            .willReturn(List.of());

        // when
        List<ProjectApplicationSummaryInfo> result = sut.listMyApplications(query);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("listMyApplications_decisionDeadline_전이면_지원서_상태를_null_로_반환")
    void masksStatusBeforeDecisionDeadlineInMyApplications() {
        // given
        Project project = createProject(1L, "프로젝트A", "thumb-1", 99L);
        ProjectMatchingRound round = createMatchingRound(
            7L, MatchingType.PLAN_DEVELOPER, MatchingPhase.FIRST);
        ProjectApplication application = createApplication(
            55L, project, round, ProjectApplicationStatus.SUBMITTED);

        GetMyProjectApplicationsQuery query = queryOf(null);
        given(getChallengerUseCase.findByMemberIdAndGisuId(REQUESTER_ID, GISU_ID))
            .willReturn(Optional.of(challengerOf(ChallengerPart.WEB)));
        given(loadProjectApplicationPort.searchMyApplications(
            eq(REQUESTER_ID), eq(GISU_ID), eq(MatchingType.PLAN_DEVELOPER), eq(null)))
            .willReturn(List.of(application));

        // when
        List<ProjectApplicationSummaryInfo> result = sut.listMyApplications(query);

        // then
        assertThat(result).hasSize(1);
        ProjectApplicationSummaryInfo info = result.get(0);
        assertThat(info.id()).isEqualTo(55L);
        assertThat(info.projectId()).isEqualTo(1L);
        assertThat(info.matchingRoundId()).isEqualTo(7L);
        assertThat(info.status()).isNull();
        assertThat(info.applicantMemberId()).isEqualTo(REQUESTER_ID);
    }

    @Test
    @DisplayName("listMyApplications_decisionDeadline_후면_상태와_무관하게_상태_반환")
    void statusVisibleAfterDecisionDeadlineRegardlessOfApplicationStatus() {
        // given
        Project project = createProject(1L, "프로젝트A", "thumb-1", 99L);
        ProjectMatchingRound round = createMatchingRound(
            7L, MatchingType.PLAN_DEVELOPER, MatchingPhase.FIRST);
        markDecisionDeadlinePassed(round);
        ProjectApplication submittedApplication = createApplication(
            55L, project, round, ProjectApplicationStatus.SUBMITTED);
        ProjectApplication approvedApplication = createApplication(
            56L, project, round, ProjectApplicationStatus.APPROVED);
        ProjectApplication rejectedApplication = createApplication(
            57L, project, round, ProjectApplicationStatus.REJECTED);

        GetMyProjectApplicationsQuery query = queryOf(null);
        given(getChallengerUseCase.findByMemberIdAndGisuId(REQUESTER_ID, GISU_ID))
            .willReturn(Optional.of(challengerOf(ChallengerPart.WEB)));
        given(loadProjectApplicationPort.searchMyApplications(
            eq(REQUESTER_ID), eq(GISU_ID), eq(MatchingType.PLAN_DEVELOPER), eq(null)))
            .willReturn(List.of(submittedApplication, approvedApplication, rejectedApplication));

        // when
        List<ProjectApplicationSummaryInfo> result = sut.listMyApplications(query);

        // then
        assertThat(result)
            .extracting(ProjectApplicationSummaryInfo::status)
            .containsExactly(
                ProjectApplicationStatus.SUBMITTED,
                ProjectApplicationStatus.APPROVED,
                ProjectApplicationStatus.REJECTED
            );
    }

    @Test
    @DisplayName("listMyApplications_decisionDeadline_전이면_APPROVED_지원서도_상태_null")
    void approvedStatusHiddenBeforeDecisionDeadline() {
        // given
        Project project = createProject(1L, "프로젝트A", "thumb-1", 99L);
        ProjectMatchingRound round = createMatchingRound(
            7L, MatchingType.PLAN_DEVELOPER, MatchingPhase.FIRST);
        ProjectApplication application = createApplication(
            55L, project, round, ProjectApplicationStatus.APPROVED);

        GetMyProjectApplicationsQuery query = queryOf(null);
        given(getChallengerUseCase.findByMemberIdAndGisuId(REQUESTER_ID, GISU_ID))
            .willReturn(Optional.of(challengerOf(ChallengerPart.WEB)));
        given(loadProjectApplicationPort.searchMyApplications(
            eq(REQUESTER_ID), eq(GISU_ID), eq(MatchingType.PLAN_DEVELOPER), eq(null)))
            .willReturn(List.of(application));

        // when
        List<ProjectApplicationSummaryInfo> result = sut.listMyApplications(query);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).status()).isNull();
    }

    // ============================================================
    //          searchByProject (PM/운영진 지원자 목록 조회) 테스트
    // ============================================================

    @Test
    @DisplayName("searchByProject_DRAFT_상태_필터를_사용하면_도메인_예외")
    void searchByProject_DRAFT_필터_금지() {
        // given & when & then -- Query record compact constructor 에서 차단된다
        assertThatThrownBy(() -> SearchProjectApplicationsQuery.builder()
            .requesterMemberId(REQUESTER_ID)
            .projectId(1L)
            .status(ProjectApplicationStatus.DRAFT)
            .build())
            .isInstanceOf(ProjectDomainException.class);
    }

    @Test
    @DisplayName("searchByProject_권한_scope_가_None_이면_빈_리스트_위장")
    void searchByProject_권한_없음_빈_리스트() {
        // given
        Project project = createProject(1L, "프로젝트A", null, 99L);
        SearchProjectApplicationsQuery query = SearchProjectApplicationsQuery.builder()
            .requesterMemberId(REQUESTER_ID).projectId(1L).build();

        given(loadProjectPort.getById(1L)).willReturn(project);
        given(accessScopeResolver.resolveForProjectApplicantList(REQUESTER_ID, project))
            .willReturn(new ProjectApplicationAccessScope.None());

        // when
        List<ProjectApplicationSummaryInfo> result = sut.searchByProject(query);

        // then
        assertThat(result).isEmpty();
        verify(loadProjectApplicationPort, never())
            .searchProjectApplications(any(), any(), any(), any(), anyBoolean());
    }

    @Test
    @DisplayName("searchByProject_지원서가_없으면_빈_리스트")
    void searchByProject_빈_지원서_빈_리스트() {
        // given
        Project project = createProject(1L, "프로젝트A", null, 99L);
        SearchProjectApplicationsQuery query = SearchProjectApplicationsQuery.builder()
            .requesterMemberId(REQUESTER_ID).projectId(1L).build();

        given(loadProjectPort.getById(1L)).willReturn(project);
        given(accessScopeResolver.resolveForProjectApplicantList(REQUESTER_ID, project))
            .willReturn(new ProjectApplicationAccessScope.ProjectScoped(1L));
        given(loadProjectApplicationPort.searchProjectApplications(eq(1L), isNull(), isNull(), any(), eq(false)))
            .willReturn(List.of());

        // when
        List<ProjectApplicationSummaryInfo> result = sut.searchByProject(query);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("searchByProject_엔티티가_있으면_ProjectApplicationSummaryInfo_로_매핑")
    void searchByProject_자원_매핑_정상() {
        // given
        Project project = createProject(1L, "프로젝트A", null, 99L);
        ProjectMatchingRound round = createMatchingRound(
            7L, MatchingType.PLAN_DEVELOPER, MatchingPhase.FIRST);
        ProjectApplication application = createSubmittedApplication(
            55L, project, round, 200L, ProjectApplicationStatus.APPROVED);
        given(checkPermissionUseCase.loadSubject(REQUESTER_ID)).willReturn(
            SubjectAttributes.builder().memberId(REQUESTER_ID)
                .systemRoles(Set.of(SystemRoleType.SUPER_ADMIN)).build());

        SearchProjectApplicationsQuery query = SearchProjectApplicationsQuery.builder()
            .requesterMemberId(REQUESTER_ID).projectId(1L).build();

        given(loadProjectPort.getById(1L)).willReturn(project);
        given(accessScopeResolver.resolveForProjectApplicantList(REQUESTER_ID, project))
            .willReturn(new ProjectApplicationAccessScope.ProjectScoped(1L));
        given(loadProjectApplicationPort.searchProjectApplications(eq(1L), isNull(), isNull(), any(), eq(false)))
            .willReturn(List.of(application));

        // when
        List<ProjectApplicationSummaryInfo> result = sut.searchByProject(query);

        // then
        assertThat(result).hasSize(1);
        ProjectApplicationSummaryInfo info = result.get(0);
        assertThat(info.id()).isEqualTo(55L);
        assertThat(info.applicantMemberId()).isEqualTo(200L);
        assertThat(info.projectId()).isEqualTo(1L);
        assertThat(info.matchingRoundId()).isEqualTo(7L);
        assertThat(info.status()).isEqualTo(ProjectApplicationStatus.APPROVED);
    }

    @Test
    @DisplayName("searchByProject_중앙총괄단_scope면_진행_중_차수_지원서까지_조회")
    void searchByProject_중앙총괄단은_진행중_차수까지_조회() {
        // given
        Project project = createProject(1L, "프로젝트A", null, 99L);
        ProjectMatchingRound round = createMatchingRound(
            7L, MatchingType.PLAN_DEVELOPER, MatchingPhase.FIRST);
        ReflectionTestUtils.setField(round, "endsAt", java.time.Instant.now().plusSeconds(3_600));
        ProjectApplication application = createSubmittedApplication(
            55L, project, round, 200L, ProjectApplicationStatus.SUBMITTED);
        given(checkPermissionUseCase.loadSubject(REQUESTER_ID))
            .willReturn(SubjectAttributes.builder().memberId(REQUESTER_ID).build());

        SearchProjectApplicationsQuery query = SearchProjectApplicationsQuery.builder()
            .requesterMemberId(REQUESTER_ID).projectId(1L).build();

        given(loadProjectPort.getById(1L)).willReturn(project);
        given(accessScopeResolver.resolveForProjectApplicantList(REQUESTER_ID, project))
            .willReturn(new ProjectApplicationAccessScope.ProjectScoped(1L, true));
        given(loadProjectApplicationPort.searchProjectApplications(eq(1L), isNull(), isNull(), any(), eq(true)))
            .willReturn(List.of(application));

        // when
        List<ProjectApplicationSummaryInfo> result = sut.searchByProject(query);

        // then
        assertThat(result).hasSize(1);
        verify(loadProjectApplicationPort).searchProjectApplications(eq(1L), isNull(), isNull(), any(), eq(true));
    }

    @Test
    @DisplayName("searchByProject_차수는_repository에_전달하고_상태는_표시_결과로_필터링한다")
    void searchByProject_필터_전달() {
        // given
        Project project = createProject(1L, "프로젝트A", null, 99L);
        SearchProjectApplicationsQuery query = SearchProjectApplicationsQuery.builder()
            .requesterMemberId(REQUESTER_ID)
            .projectId(1L)
            .matchingRoundId(7L)
            .status(ProjectApplicationStatus.APPROVED)
            .build();

        given(loadProjectPort.getById(1L)).willReturn(project);
        given(accessScopeResolver.resolveForProjectApplicantList(REQUESTER_ID, project))
            .willReturn(new ProjectApplicationAccessScope.ProjectScoped(1L));
        given(loadProjectApplicationPort.searchProjectApplications(
            eq(1L), eq(7L), isNull(), any(), eq(false)))
            .willReturn(List.of());

        // when
        sut.searchByProject(query);

        // then
        verify(loadProjectApplicationPort).searchProjectApplications(
            eq(1L), eq(7L), isNull(), any(), eq(false));
    }

    @Test
    @DisplayName("searchByProjects_요청한_projectId_key를_보존하고_권한_없는_프로젝트와_없는_프로젝트는_빈_리스트")
    void searchByProjects_키_보존_및_빈_리스트() {
        // given
        Project projectA = createProject(1L, "프로젝트A", null, 99L);
        Project projectB = createProject(2L, "프로젝트B", null, 88L);
        ProjectMatchingRound round = createMatchingRound(
            7L, MatchingType.PLAN_DEVELOPER, MatchingPhase.FIRST);
        ProjectApplication application = createSubmittedApplication(
            55L, projectA, round, 200L, ProjectApplicationStatus.SUBMITTED);
        given(checkPermissionUseCase.loadSubject(REQUESTER_ID))
            .willReturn(SubjectAttributes.builder().memberId(REQUESTER_ID).build());
        SearchProjectApplicationsBatchQuery query = SearchProjectApplicationsBatchQuery.builder()
            .requesterMemberId(REQUESTER_ID)
            .projectIds(List.of(1L, 2L, 3L))
            .build();

        given(loadProjectPort.listByIds(List.of(1L, 2L, 3L)))
            .willReturn(List.of(projectA, projectB));
        given(accessScopeResolver.resolveForProjectApplicantLists(REQUESTER_ID, List.of(projectA, projectB)))
            .willReturn(Map.of(
                1L, new ProjectApplicationAccessScope.ProjectScoped(1L),
                2L, new ProjectApplicationAccessScope.None()
            ));
        given(loadProjectApplicationPort.searchProjectApplicationsByProjectIds(
            eq(Set.of(1L)), eq(Set.of()), isNull(), isNull(), any()))
            .willReturn(List.of(application));

        // when
        Map<Long, List<ProjectApplicationSummaryInfo>> result = sut.searchByProjects(query);

        // then
        assertThat(result.keySet()).containsExactly(1L, 2L, 3L);
        assertThat(result.get(1L)).extracting(ProjectApplicationSummaryInfo::id).containsExactly(55L);
        assertThat(result.get(2L)).isEmpty();
        assertThat(result.get(3L)).isEmpty();
        verify(loadProjectApplicationPort).searchProjectApplicationsByProjectIds(
            eq(Set.of(1L)), eq(Set.of()), isNull(), isNull(), any());
    }

    @Test
    @DisplayName("searchByProjects_ProjectScoped_includeOngoing_true인_프로젝트만_진행중_차수_포함_목록으로_전달")
    void searchByProjects_includeOngoing_프로젝트만_전달() {
        // given
        Project projectA = createProject(1L, "프로젝트A", null, 99L);
        Project projectB = createProject(2L, "프로젝트B", null, 88L);
        SearchProjectApplicationsBatchQuery query = SearchProjectApplicationsBatchQuery.builder()
            .requesterMemberId(REQUESTER_ID)
            .projectIds(List.of(1L, 2L))
            .matchingRoundId(7L)
            .status(ProjectApplicationStatus.APPROVED)
            .build();

        given(loadProjectPort.listByIds(List.of(1L, 2L)))
            .willReturn(List.of(projectA, projectB));
        given(accessScopeResolver.resolveForProjectApplicantLists(REQUESTER_ID, List.of(projectA, projectB)))
            .willReturn(Map.of(
                1L, new ProjectApplicationAccessScope.ProjectScoped(1L),
                2L, new ProjectApplicationAccessScope.ProjectScoped(2L, true)
            ));
        given(loadProjectApplicationPort.searchProjectApplicationsByProjectIds(
            eq(Set.of(1L, 2L)),
            eq(Set.of(2L)),
            eq(7L),
            isNull(),
            any()
        )).willReturn(List.of());

        // when
        sut.searchByProjects(query);

        // then
        verify(loadProjectApplicationPort).searchProjectApplicationsByProjectIds(
            eq(Set.of(1L, 2L)),
            eq(Set.of(2L)),
            eq(7L),
            isNull(),
            any()
        );
    }

    @Test
    @DisplayName("searchByProjects_DRAFT_상태_필터를_사용하면_도메인_예외")
    void searchByProjects_DRAFT_필터_금지() {
        assertThatThrownBy(() -> SearchProjectApplicationsBatchQuery.builder()
            .requesterMemberId(REQUESTER_ID)
            .projectIds(List.of(1L))
            .status(ProjectApplicationStatus.DRAFT)
            .build())
            .isInstanceOf(ProjectDomainException.class);
    }

    @ParameterizedTest
    @EnumSource(value = ChallengerRoleType.class, names = {
        "CENTRAL_OPERATING_TEAM_MEMBER", "CENTRAL_EDUCATION_TEAM_MEMBER",
        "SCHOOL_PRESIDENT", "SCHOOL_VICE_PRESIDENT", "SCHOOL_PART_LEADER", "SCHOOL_ETC_ADMIN"
    })
    void 판정_권한이_없는_운영진은_이번_차수만_대기로_조회한다(ChallengerRoleType roleType) {
        // given - 프로젝트 상태와 무관하게 차수별 결정 기간으로만 판단한다.
        Project project = createProject(1L, "프로젝트", null, 99L);
        ReflectionTestUtils.setField(project, "status", ProjectStatus.ABORTED);
        ProjectMatchingRound previousRound = applicantListRound(7L, MatchingPhase.FIRST,
            LIST_NOW.minusSeconds(7200), LIST_NOW.minusSeconds(3600));
        ProjectMatchingRound currentRound = applicantListRound(8L, MatchingPhase.SECOND,
            LIST_NOW.minusSeconds(3600), LIST_NOW.plusSeconds(3600));
        List<ProjectApplication> applications = List.of(
            createSubmittedApplication(51L, project, previousRound, 201L, ProjectApplicationStatus.APPROVED),
            createSubmittedApplication(52L, project, currentRound, 202L, ProjectApplicationStatus.APPROVED),
            createSubmittedApplication(53L, project, currentRound, 203L, ProjectApplicationStatus.REJECTED),
            createSubmittedApplication(54L, project, currentRound, 204L, ProjectApplicationStatus.SUBMITTED)
        );
        SubjectAttributes subject = SubjectAttributes.builder().memberId(REQUESTER_ID)
            .roleAttributes(List.of(new RoleAttribute(roleType, roleType.organizationType(), 1L, null, GISU_ID)))
            .build();
        givenApplicantListQueries(project, applications, subject, LIST_NOW);

        // when
        List<ProjectApplicationSummaryInfo> single = sut.searchByProject(applicantListQuery(null));
        List<ProjectApplicationSummaryInfo> batch = sut.searchByProjects(applicantListBatchQuery(null)).get(1L);

        // then
        assertThat(single).isEqualTo(batch);
        assertThat(single).extracting(ProjectApplicationSummaryInfo::status).containsExactly(
            ProjectApplicationStatus.APPROVED, ProjectApplicationStatus.SUBMITTED,
            ProjectApplicationStatus.SUBMITTED, ProjectApplicationStatus.SUBMITTED);
        assertThat(single.get(0).statusChangedAt()).isEqualTo(applications.get(0).getStatusChangedAt());
        assertThat(single.subList(1, 4)).allSatisfy(summary -> assertThat(summary.statusChangedAt()).isNull());
        assertThat(applications).extracting(ProjectApplication::getStatus).containsExactly(
            ProjectApplicationStatus.APPROVED, ProjectApplicationStatus.APPROVED,
            ProjectApplicationStatus.REJECTED, ProjectApplicationStatus.SUBMITTED);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "PM", "SUPER_ADMIN", "CENTRAL_PRESIDENT", "CENTRAL_VICE_PRESIDENT", "CHAPTER_PRESIDENT"
    })
    void 판정_권한자는_결정_기간에도_실제_합불_결과를_조회한다(String authority) {
        // given
        Project project = createProject(1L, "프로젝트", null, authority.equals("PM") ? REQUESTER_ID : 99L);
        ProjectMatchingRound round = applicantListRound(7L, MatchingPhase.FIRST,
            LIST_NOW.minusSeconds(3600), LIST_NOW.plusSeconds(3600));
        List<ProjectApplication> applications = List.of(
            createSubmittedApplication(51L, project, round, 201L, ProjectApplicationStatus.APPROVED),
            createSubmittedApplication(52L, project, round, 202L, ProjectApplicationStatus.REJECTED)
        );
        SubjectAttributes.SubjectAttributesBuilder subject = SubjectAttributes.builder().memberId(REQUESTER_ID);
        if (authority.equals("SUPER_ADMIN")) {
            subject.systemRoles(Set.of(SystemRoleType.SUPER_ADMIN));
        } else if (!authority.equals("PM")) {
            ChallengerRoleType role = ChallengerRoleType.valueOf(authority);
            subject.roleAttributes(List.of(
                new RoleAttribute(role, role.organizationType(), project.getChapterId(), null, GISU_ID)));
        }
        givenApplicantListQueries(project, applications, subject.build(), LIST_NOW);

        // when
        List<ProjectApplicationSummaryInfo> single = sut.searchByProject(applicantListQuery(null));
        List<ProjectApplicationSummaryInfo> batch = sut.searchByProjects(applicantListBatchQuery(null)).get(1L);

        // then
        assertThat(single).isEqualTo(batch);
        assertThat(single).extracting(ProjectApplicationSummaryInfo::status)
            .containsExactly(ProjectApplicationStatus.APPROVED, ProjectApplicationStatus.REJECTED);
        assertThat(single).extracting(ProjectApplicationSummaryInfo::statusChangedAt)
            .containsExactly(applications.get(0).getStatusChangedAt(), applications.get(1).getStatusChangedAt());
    }

    @ParameterizedTest
    @CsvSource({"0, APPROVED", "1, SUBMITTED", "3600, SUBMITTED", "3601, APPROVED"})
    void 결과_가림은_지원_마감_직후부터_결정_마감까지_적용한다(
        long secondsAfterEnd, ProjectApplicationStatus expectedStatus
    ) {
        // given
        Project project = createProject(1L, "프로젝트", null, 99L);
        ProjectMatchingRound round = applicantListRound(7L, MatchingPhase.FIRST,
            LIST_NOW, LIST_NOW.plusSeconds(3600));
        ProjectApplication application = createSubmittedApplication(
            51L, project, round, 201L, ProjectApplicationStatus.APPROVED);
        givenApplicantListQueries(project, List.of(application),
            SubjectAttributes.builder().memberId(REQUESTER_ID).build(), LIST_NOW.plusSeconds(secondsAfterEnd));

        // when
        List<ProjectApplicationSummaryInfo> single = sut.searchByProject(applicantListQuery(null));
        List<ProjectApplicationSummaryInfo> batch = sut.searchByProjects(applicantListBatchQuery(null)).get(1L);

        // then
        assertThat(single).isEqualTo(batch);
        assertThat(single).extracting(ProjectApplicationSummaryInfo::status).containsExactly(expectedStatus);
    }

    @ParameterizedTest
    @EnumSource(value = ProjectApplicationStatus.class, names = {"SUBMITTED", "APPROVED", "REJECTED"})
    void 상태_필터로_가려진_실제_합불_결과를_알아낼_수_없다(ProjectApplicationStatus filter) {
        // given
        Project project = createProject(1L, "프로젝트", null, 99L);
        ProjectMatchingRound previousRound = applicantListRound(7L, MatchingPhase.FIRST,
            LIST_NOW.minusSeconds(7200), LIST_NOW.minusSeconds(3600));
        ProjectMatchingRound currentRound = applicantListRound(8L, MatchingPhase.SECOND,
            LIST_NOW.minusSeconds(3600), LIST_NOW.plusSeconds(3600));
        List<ProjectApplication> applications = List.of(
            createSubmittedApplication(51L, project, previousRound, 201L, ProjectApplicationStatus.APPROVED),
            createSubmittedApplication(52L, project, currentRound, 202L, ProjectApplicationStatus.APPROVED),
            createSubmittedApplication(53L, project, currentRound, 203L, ProjectApplicationStatus.REJECTED),
            createSubmittedApplication(54L, project, currentRound, 204L, ProjectApplicationStatus.SUBMITTED)
        );
        givenApplicantListQueries(project, applications,
            SubjectAttributes.builder().memberId(REQUESTER_ID).build(), LIST_NOW);

        // when
        List<ProjectApplicationSummaryInfo> single = sut.searchByProject(applicantListQuery(filter));
        List<ProjectApplicationSummaryInfo> batch = sut.searchByProjects(applicantListBatchQuery(filter)).get(1L);

        // then
        assertThat(single).isEqualTo(batch);
        List<Long> expectedIds = switch (filter) {
            case SUBMITTED -> List.of(52L, 53L, 54L);
            case APPROVED -> List.of(51L);
            default -> List.of();
        };
        assertThat(single).extracting(ProjectApplicationSummaryInfo::id).containsExactlyElementsOf(expectedIds);
    }

    // ============================================================
    //                getDetail (지원서 단건 상세 조회) 테스트
    // ============================================================

    @Test
    @DisplayName("getDetail_application_미존재시_PROJECT_APPLICATION_NOT_FOUND")
    void getDetail_미존재() {
        // given
        GetProjectApplicationDetailQuery query = detailQuery(1L, 999L);
        given(loadProjectApplicationPort.findByIdWithDetails(999L))
            .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> sut.getDetail(query))
            .isInstanceOf(ProjectDomainException.class)
            .hasFieldOrPropertyWithValue("baseCode", ProjectErrorCode.PROJECT_APPLICATION_NOT_FOUND);
    }

    @Test
    @DisplayName("getDetail_application_은_존재하나_path_projectId_와_form_project_id_가_다르면_NOT_FOUND_위장")
    void getDetail_정합성_위반() {
        // given - application 의 form.project.id 는 1L 인데 path 는 2L
        Project project = createProject(1L, "프로젝트A", null, 99L);
        ProjectMatchingRound round = createMatchingRound(
            7L, MatchingType.PLAN_DESIGN, MatchingPhase.FIRST);
        ProjectApplication application = createSubmittedApplication(
            55L, project, round, 200L, ProjectApplicationStatus.SUBMITTED);

        GetProjectApplicationDetailQuery query = detailQuery(2L, 55L);
        given(loadProjectApplicationPort.findByIdWithDetails(55L))
            .willReturn(Optional.of(application));

        // when & then
        assertThatThrownBy(() -> sut.getDetail(query))
            .isInstanceOf(ProjectDomainException.class)
            .hasFieldOrPropertyWithValue("baseCode", ProjectErrorCode.PROJECT_APPLICATION_NOT_FOUND);
        verify(getChallengerUseCase, never()).findByMemberIdAndGisuId(any(), any());
    }

    @Test
    @DisplayName("getDetail_지원_진행_중인_차수면_APPLICANTS_NOT_VIEWABLE_로_차단")
    void getDetail_지원_진행_중_차단() {
        // given - endsAt 이 미래라 아직 지원 기간 중
        Project project = createProject(1L, "프로젝트A", null, 99L);
        ProjectMatchingRound round = createMatchingRound(
            7L, MatchingType.PLAN_DESIGN, MatchingPhase.FIRST);
        ReflectionTestUtils.setField(round, "endsAt", java.time.Instant.now().plusSeconds(3_600));
        ProjectApplication application = createSubmittedApplication(
            55L, project, round, 200L, ProjectApplicationStatus.SUBMITTED);

        GetProjectApplicationDetailQuery query = detailQuery(1L, 55L);
        given(loadProjectApplicationPort.findByIdWithDetails(55L))
            .willReturn(Optional.of(application));

        // when & then
        assertThatThrownBy(() -> sut.getDetail(query))
            .isInstanceOf(ProjectDomainException.class)
            .hasFieldOrPropertyWithValue("baseCode",
                ProjectErrorCode.PROJECT_MATCHING_ROUND_APPLICANTS_NOT_VIEWABLE);
        verify(getChallengerUseCase, never()).findByMemberIdAndGisuId(any(), any());
    }

    @Test
    @DisplayName("getDetail_PM_scope는_목록만_실시간이라_지원_진행_중_내용_열람은_APPLICANTS_NOT_VIEWABLE")
    void getDetail_PM은_지원_진행_중_내용_열람_차단() {
        // given - PO/보조 PM 범위: 목록은 진행 중 차수 포함, 내용 열람은 지원 마감 후
        Project project = createProject(1L, "프로젝트A", null, 99L);
        ProjectMatchingRound round = createMatchingRound(
            7L, MatchingType.PLAN_DESIGN, MatchingPhase.FIRST);
        ReflectionTestUtils.setField(round, "endsAt", java.time.Instant.now().plusSeconds(3_600));
        ProjectApplication application = createSubmittedApplication(
            55L, project, round, 200L, ProjectApplicationStatus.SUBMITTED);

        GetProjectApplicationDetailQuery query = detailQuery(1L, 55L);
        given(loadProjectApplicationPort.findByIdWithDetails(55L))
            .willReturn(Optional.of(application));
        given(accessScopeResolver.resolveForProjectApplicantList(REQUESTER_ID, project))
            .willReturn(new ProjectApplicationAccessScope.ProjectScoped(1L, true, false));

        // when & then
        assertThatThrownBy(() -> sut.getDetail(query))
            .isInstanceOf(ProjectDomainException.class)
            .hasFieldOrPropertyWithValue("baseCode",
                ProjectErrorCode.PROJECT_MATCHING_ROUND_APPLICANTS_NOT_VIEWABLE);
        verify(getChallengerUseCase, never()).findByMemberIdAndGisuId(any(), any());
    }

    @Test
    @DisplayName("searchByProject_PM_scope면_진행_중_차수_지원서까지_목록으로_조회")
    void searchByProject_PM은_진행_중_차수_목록_조회() {
        // given
        Project project = createProject(1L, "프로젝트A", null, 99L);
        SearchProjectApplicationsQuery query = SearchProjectApplicationsQuery.builder()
            .requesterMemberId(REQUESTER_ID).projectId(1L).build();

        given(loadProjectPort.getById(1L)).willReturn(project);
        given(accessScopeResolver.resolveForProjectApplicantList(REQUESTER_ID, project))
            .willReturn(new ProjectApplicationAccessScope.ProjectScoped(1L, true, false));
        given(loadProjectApplicationPort.searchProjectApplications(eq(1L), isNull(), isNull(), any(), eq(true)))
            .willReturn(List.of());

        // when
        sut.searchByProject(query);

        // then
        verify(loadProjectApplicationPort).searchProjectApplications(eq(1L), isNull(), isNull(), any(), eq(true));
    }

    @Test
    @DisplayName("getDetail_중앙총괄단_scope면_지원_진행_중인_차수도_조회")
    void getDetail_중앙총괄단은_지원_진행_중에도_조회() {
        // given - endsAt 이 미래라 아직 지원 기간 중이지만 중앙 총괄단 scope 로 통과
        Project project = createProject(1L, "프로젝트A", null, 99L);
        ProjectMatchingRound round = createMatchingRound(
            7L, MatchingType.PLAN_DESIGN, MatchingPhase.FIRST);
        ReflectionTestUtils.setField(round, "endsAt", java.time.Instant.now().plusSeconds(3_600));
        ProjectApplication application = createApplicationWithFormResponse(
            55L, project, round, 200L, ProjectApplicationStatus.SUBMITTED, 123L);

        GetProjectApplicationDetailQuery query = detailQuery(1L, 55L);
        given(loadProjectApplicationPort.findByIdWithDetails(55L))
            .willReturn(Optional.of(application));
        given(accessScopeResolver.resolveForProjectApplicantList(REQUESTER_ID, project))
            .willReturn(new ProjectApplicationAccessScope.ProjectScoped(1L, true));
        given(getChallengerUseCase.findByMemberIdAndGisuId(200L, GISU_ID))
            .willReturn(Optional.of(challengerInfoOf(200L, ChallengerPart.DESIGN)));
        given(loadProjectApplicationFormPolicyPort.listByApplicationFormId(any()))
            .willReturn(List.of());
        given(getFormResponseUseCase.findResponseWithAnswers(123L))
            .willReturn(Optional.of(FormResponseWithAnswersInfo.builder()
                .id(123L).formId(7L).respondentMemberId(200L)
                .status(FormResponseStatus.SUBMITTED)
                .answers(List.of())
                .build()));
        given(getFormUseCase.getFormWithStructureByQuestionIds(eq(7L), anySet())).willReturn(
            FormWithStructureInfo.builder().formId(7L).sections(List.of()).build());

        // when
        ProjectApplicationDetailInfo result = sut.getDetail(query);

        // then
        assertThat(result.applicationId()).isEqualTo(55L);
    }

    @Test
    @DisplayName("getDetail_섹션_파트_정책이_바뀌어도_답변이_있는_섹션은_그대로_보여준다")
    void getDetail_정책_변경과_무관하게_답변한_섹션_표시() {
        // given - 디자이너가 답변한 섹션의 정책이 이후 '개발 파트 전용' 으로 바뀐 상황
        Project project = createProject(1L, "프로젝트A", null, 99L);
        ProjectMatchingRound round = createMatchingRound(
            7L, MatchingType.PLAN_DESIGN, MatchingPhase.FIRST);
        markDecisionDeadlinePassed(round);
        ProjectApplication application = createApplicationWithFormResponse(
            55L, project, round, 200L, ProjectApplicationStatus.SUBMITTED, 123L);

        GetProjectApplicationDetailQuery query = detailQuery(1L, 55L);
        given(loadProjectApplicationPort.findByIdWithDetails(55L))
            .willReturn(Optional.of(application));
        given(accessScopeResolver.resolveForProjectApplicantList(REQUESTER_ID, project))
            .willReturn(new ProjectApplicationAccessScope.ProjectScoped(1L, true));
        given(getChallengerUseCase.findByMemberIdAndGisuId(200L, GISU_ID))
            .willReturn(Optional.of(challengerInfoOf(200L, ChallengerPart.DESIGN)));
        given(loadProjectApplicationFormPolicyPort.listByApplicationFormId(any()))
            .willReturn(List.of(ProjectApplicationFormPolicy.createForParts(
                application.getApplicationForm(), 10L, Set.of(ChallengerPart.WEB))));
        given(getFormResponseUseCase.findResponseWithAnswers(123L))
            .willReturn(Optional.of(FormResponseWithAnswersInfo.builder()
                .id(123L).formId(7L).respondentMemberId(200L)
                .status(FormResponseStatus.SUBMITTED)
                .answers(List.of())
                .build()));
        given(getFormUseCase.getFormWithStructureByQuestionIds(eq(7L), anySet())).willReturn(
            FormWithStructureInfo.builder().formId(7L).sections(List.of(
                FormWithStructureInfo.SectionWithQuestions.builder()
                    .sectionId(10L).title("포트폴리오").orderNo(1L)
                    .questions(List.of(FormWithStructureInfo.QuestionWithOptions.builder()
                        .questionId(100L).title("포트폴리오 링크").type(QuestionType.SHORT_TEXT)
                        .isRequired(true).orderNo(1L).options(List.of())
                        .build()))
                    .build()
            )).build());

        // when
        ProjectApplicationDetailInfo result = sut.getDetail(query);

        // then - 현재 정책(WEB 전용)으로 가리지 않고 답변한 섹션을 그대로 돌려준다
        assertThat(result.formStructure().sections()).hasSize(1);
        assertThat(result.formStructure().sections().get(0).sectionId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("getDetail_지원자가_해당_기수_챌린저가_아니면_NOT_FOUND_위장")
    void getDetail_챌린저_누락() {
        // given
        Project project = createProject(1L, "프로젝트A", null, 99L);
        ProjectMatchingRound round = createMatchingRound(
            7L, MatchingType.PLAN_DESIGN, MatchingPhase.FIRST);
        ProjectApplication application = createSubmittedApplication(
            55L, project, round, 200L, ProjectApplicationStatus.SUBMITTED);

        GetProjectApplicationDetailQuery query = detailQuery(1L, 55L);
        given(loadProjectApplicationPort.findByIdWithDetails(55L))
            .willReturn(Optional.of(application));
        given(getChallengerUseCase.findByMemberIdAndGisuId(200L, GISU_ID))
            .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> sut.getDetail(query))
            .isInstanceOf(ProjectDomainException.class)
            .hasFieldOrPropertyWithValue("baseCode", ProjectErrorCode.PROJECT_APPLICATION_NOT_FOUND);
        verify(getFormUseCase, never()).getFormWithStructureByQuestionIds(any(), any());
    }

    @Test
    @DisplayName("getDetail_타인_조회는_결과_공개_전에도_합격_상태와_지원서_내용을_반환")
    void getDetail_타인_조회는_결과_공개_전에도_합격_상태를_반환() {
        // given
        Project project = createProject(1L, "프로젝트A", null, 99L);
        ProjectMatchingRound round = createMatchingRound(
            7L, MatchingType.PLAN_DESIGN, MatchingPhase.FIRST);
        markDecisionDeadlinePassed(round);
        ReflectionTestUtils.setField(round, "resultPublishAt", java.time.Instant.now().plusSeconds(600));
        ProjectApplication application = createApplicationWithFormResponse(
            55L, project, round, 200L, ProjectApplicationStatus.APPROVED, 123L);

        GetProjectApplicationDetailQuery query = detailQuery(1L, 55L);
        given(loadProjectApplicationPort.findByIdWithDetails(55L))
            .willReturn(Optional.of(application));
        given(getChallengerUseCase.findByMemberIdAndGisuId(200L, GISU_ID))
            .willReturn(Optional.of(challengerInfoOf(200L, ChallengerPart.DESIGN)));

        FormWithStructureInfo formStructure = FormWithStructureInfo.builder()
            .formId(7L)
            .title("지원폼")
            .description(null)
            .sections(List.of())
            .build();
        given(loadProjectApplicationFormPolicyPort.listByApplicationFormId(any()))
            .willReturn(List.of());

        AnswerInfo textAnswer = AnswerInfo.builder()
            .id(501L)
            .formResponseId(123L)
            .questionId(11L)
            .answeredAsType(QuestionType.SHORT_TEXT)
            .textValue("이방토/이예원")
            .selectedOptions(List.of())
            .fileIds(null)
            .times(null)
            .build();
        AnswerInfo fileAnswer = AnswerInfo.builder()
            .id(504L)
            .formResponseId(123L)
            .questionId(16L)
            .answeredAsType(QuestionType.PORTFOLIO)
            .textValue(null)
            .selectedOptions(List.of())
            .fileIds(java.util.Set.of("f-abc"))
            .times(null)
            .build();
        FormResponseWithAnswersInfo formResponseWithAnswers = FormResponseWithAnswersInfo.builder()
            .id(123L)
            .formId(7L)
            .respondentMemberId(200L)
            .status(FormResponseStatus.SUBMITTED)
            .submittedAt(java.time.Instant.parse("2026-04-22T01:30:00Z"))
            .submittedIp("127.0.0.1")
            .lastSavedAt(java.time.Instant.parse("2026-04-22T01:30:00Z"))
            .createdAt(java.time.Instant.parse("2026-04-22T01:00:00Z"))
            .updatedAt(java.time.Instant.parse("2026-04-22T01:30:00Z"))
            .answers(List.of(textAnswer, fileAnswer))
            .build();
        given(getFormResponseUseCase.findResponseWithAnswers(123L))
            .willReturn(Optional.of(formResponseWithAnswers));
        given(getFormUseCase.getFormWithStructureByQuestionIds(eq(7L), anySet()))
            .willReturn(formStructure);

        FileInfo fileInfo = new FileInfo(
            "f-abc", "포트폴리오.pdf", FileCategory.PORTFOLIO,
            "application/pdf", 1024L, "https://cdn/f-abc",
            true, 200L, java.time.Instant.parse("2026-04-22T01:00:00Z"));
        given(getFileUseCase.findAllByIds(anyList()))
            .willReturn(Map.of("f-abc", fileInfo));

        // when
        ProjectApplicationDetailInfo result = sut.getDetail(query);

        // then
        assertThat(result.applicationId()).isEqualTo(55L);
        assertThat(result.applicantMemberId()).isEqualTo(200L);
        assertThat(result.applicantPart()).isEqualTo(ChallengerPart.DESIGN);
        assertThat(result.matchingRoundId()).isEqualTo(7L);
        assertThat(result.matchingRoundType()).isEqualTo(MatchingType.PLAN_DESIGN);
        assertThat(result.matchingRoundPhase()).isEqualTo(MatchingPhase.FIRST);
        assertThat(result.status()).isEqualTo(ProjectApplicationViewStatus.APPROVED);
        assertThat(result.formResponse().id()).isEqualTo(123L);
        assertThat(result.answersByQuestionId()).containsOnlyKeys(11L, 16L);
        assertThat(result.filesByFileId()).containsKeys("f-abc");
        assertThat(result.filesByFileId().get("f-abc").originalFileName()).isEqualTo("포트폴리오.pdf");
    }

    // DRAFT 본인 한정 위반(타인 차단) 케이스는 컨트롤러의 @CheckAccess (L2 evaluator.canRead) 가
    // 처리하므로 service 단위 테스트에서는 다루지 않는다. 해당 분기 검증은
    // ProjectApplicationPermissionEvaluatorTest 의 READ는_DRAFT_지원서를_PO여도_거부 등에서 보장.

    @Test
    @DisplayName("getDetail_DRAFT_상태이지만_지원자_본인_호출이면_정상_조회")
    void getDetail_DRAFT_본인_허용() {
        // given - applicantMemberId 와 requesterMemberId 가 동일
        Project project = createProject(1L, "프로젝트A", null, 99L);
        ProjectMatchingRound round = createMatchingRound(
            7L, MatchingType.PLAN_DESIGN, MatchingPhase.FIRST);
        ProjectApplication application = createApplicationWithFormResponse(
            55L, project, round, 200L, ProjectApplicationStatus.DRAFT, 123L);

        GetProjectApplicationDetailQuery query = detailQuery(1L, 55L, 200L);
        given(loadProjectApplicationPort.findByIdWithDetails(55L))
            .willReturn(Optional.of(application));
        given(getChallengerUseCase.findByMemberIdAndGisuId(200L, GISU_ID))
            .willReturn(Optional.of(challengerInfoOf(200L, ChallengerPart.DESIGN)));
        given(loadProjectApplicationFormPolicyPort.listByApplicationFormId(any()))
            .willReturn(List.of());
        given(getFormResponseUseCase.findResponseWithAnswers(123L))
            .willReturn(Optional.of(FormResponseWithAnswersInfo.builder()
                .id(123L).formId(7L).respondentMemberId(200L)
                .status(FormResponseStatus.DRAFT)
                .answers(List.of())
                .build()));
        given(getFormUseCase.getFormWithStructureByQuestionIds(eq(7L), anySet()))
            .willReturn(FormWithStructureInfo.builder().formId(7L).sections(List.of()).build());

        // when
        ProjectApplicationDetailInfo result = sut.getDetail(query);

        // then
        assertThat(result.applicationId()).isEqualTo(55L);
        assertThat(result.status()).isEqualTo(ProjectApplicationViewStatus.DRAFT);
    }

    @Test
    @DisplayName("getDetail_지원_진행_중이어도_지원자_본인_호출이면_정상_조회")
    void getDetail_지원_진행_중_본인_허용() {
        // given - endsAt 이 미래(지원 진행 중)이지만 본인(200==200) 호출
        Project project = createProject(1L, "프로젝트A", null, 99L);
        ProjectMatchingRound round = createMatchingRound(
            7L, MatchingType.PLAN_DESIGN, MatchingPhase.FIRST);
        ReflectionTestUtils.setField(round, "endsAt", java.time.Instant.now().plusSeconds(3_600));
        ProjectApplication application = createApplicationWithFormResponse(
            55L, project, round, 200L, ProjectApplicationStatus.SUBMITTED, 123L);

        GetProjectApplicationDetailQuery query = detailQuery(1L, 55L, 200L);
        given(loadProjectApplicationPort.findByIdWithDetails(55L))
            .willReturn(Optional.of(application));
        given(getChallengerUseCase.findByMemberIdAndGisuId(200L, GISU_ID))
            .willReturn(Optional.of(challengerInfoOf(200L, ChallengerPart.DESIGN)));
        given(loadProjectApplicationFormPolicyPort.listByApplicationFormId(any()))
            .willReturn(List.of());
        given(getFormResponseUseCase.findResponseWithAnswers(123L))
            .willReturn(Optional.of(FormResponseWithAnswersInfo.builder()
                .id(123L).formId(7L).respondentMemberId(200L)
                .status(FormResponseStatus.SUBMITTED)
                .answers(List.of())
                .build()));
        given(getFormUseCase.getFormWithStructureByQuestionIds(eq(7L), anySet()))
            .willReturn(FormWithStructureInfo.builder().formId(7L).sections(List.of()).build());

        // when
        ProjectApplicationDetailInfo result = sut.getDetail(query);

        // then
        assertThat(result.applicationId()).isEqualTo(55L);
    }

    @Test
    @DisplayName("getDetail_지원자_본인_호출은_지원서_내용을_보여주되_결과_발표_전_상태와_처리시각은_null")
    void selfDetailMasksStatusBeforeResultPublishAt() {
        // given
        Project project = createProject(1L, "프로젝트A", null, 99L);
        ProjectMatchingRound round = createMatchingRound(
            7L, MatchingType.PLAN_DESIGN, MatchingPhase.FIRST);
        markDecisionDeadlinePassed(round);
        ReflectionTestUtils.setField(round, "resultPublishAt", java.time.Instant.now().plusSeconds(600));
        round.executeAutoDecision(null);
        ProjectApplication application = createApplicationWithFormResponse(
            55L, project, round, 200L, ProjectApplicationStatus.APPROVED, 123L);
        ReflectionTestUtils.setField(application, "statusChangedAt", java.time.Instant.now().minusSeconds(300));

        GetProjectApplicationDetailQuery query = detailQuery(1L, 55L, 200L);
        given(loadProjectApplicationPort.findByIdWithDetails(55L))
            .willReturn(Optional.of(application));
        given(getChallengerUseCase.findByMemberIdAndGisuId(200L, GISU_ID))
            .willReturn(Optional.of(challengerInfoOf(200L, ChallengerPart.DESIGN)));
        given(loadProjectApplicationFormPolicyPort.listByApplicationFormId(any()))
            .willReturn(List.of());
        given(getFormResponseUseCase.findResponseWithAnswers(123L))
            .willReturn(Optional.of(FormResponseWithAnswersInfo.builder()
                .id(123L).formId(7L).respondentMemberId(200L)
                .status(FormResponseStatus.SUBMITTED)
                .answers(List.of())
                .build()));
        given(getFormUseCase.getFormWithStructureByQuestionIds(eq(7L), anySet()))
            .willReturn(FormWithStructureInfo.builder().formId(7L).sections(List.of()).build());

        // when
        ProjectApplicationDetailInfo result = sut.getDetail(query);

        // then
        assertThat(result.applicationId()).isEqualTo(55L);
        assertThat(result.status()).isNull();
        assertThat(result.formResponse().id()).isEqualTo(123L);
        assertThat(result.statusChangedAt()).isNull();
    }

    @Test
    @DisplayName("getDetail_지원자_본인_호출은_decisionDeadline_후_상태를_반환")
    void selfDetailReturnsStatusAfterDecisionDeadline() {
        // given
        Project project = createProject(1L, "프로젝트A", null, 99L);
        ProjectMatchingRound round = createMatchingRound(
            7L, MatchingType.PLAN_DESIGN, MatchingPhase.FIRST);
        markDecisionDeadlinePassed(round);
        ProjectApplication application = createApplicationWithFormResponse(
            55L, project, round, 200L, ProjectApplicationStatus.REJECTED, 123L);

        GetProjectApplicationDetailQuery query = detailQuery(1L, 55L, 200L);
        given(loadProjectApplicationPort.findByIdWithDetails(55L))
            .willReturn(Optional.of(application));
        given(getChallengerUseCase.findByMemberIdAndGisuId(200L, GISU_ID))
            .willReturn(Optional.of(challengerInfoOf(200L, ChallengerPart.DESIGN)));
        given(loadProjectApplicationFormPolicyPort.listByApplicationFormId(any()))
            .willReturn(List.of());
        given(getFormResponseUseCase.findResponseWithAnswers(123L))
            .willReturn(Optional.of(FormResponseWithAnswersInfo.builder()
                .id(123L).formId(7L).respondentMemberId(200L)
                .status(FormResponseStatus.SUBMITTED)
                .answers(List.of())
                .build()));
        given(getFormUseCase.getFormWithStructureByQuestionIds(eq(7L), anySet()))
            .willReturn(FormWithStructureInfo.builder().formId(7L).sections(List.of()).build());

        // when
        ProjectApplicationDetailInfo result = sut.getDetail(query);

        // then
        assertThat(result.applicationId()).isEqualTo(55L);
        assertThat(result.status()).isEqualTo(ProjectApplicationViewStatus.REJECTED);
        assertThat(result.formResponse().id()).isEqualTo(123L);
    }

    @Test
    @DisplayName("getDetail_formResponseId_가_dangling_이면_NOT_FOUND_위장")
    void getDetail_dangling_formResponse() {
        // given
        Project project = createProject(1L, "프로젝트A", null, 99L);
        ProjectMatchingRound round = createMatchingRound(
            7L, MatchingType.PLAN_DESIGN, MatchingPhase.FIRST);
        ProjectApplication application = createApplicationWithFormResponse(
            55L, project, round, 200L, ProjectApplicationStatus.SUBMITTED, 999L);

        GetProjectApplicationDetailQuery query = detailQuery(1L, 55L);
        given(loadProjectApplicationPort.findByIdWithDetails(55L))
            .willReturn(Optional.of(application));
        given(getChallengerUseCase.findByMemberIdAndGisuId(200L, GISU_ID))
            .willReturn(Optional.of(challengerInfoOf(200L, ChallengerPart.DESIGN)));
        given(loadProjectApplicationFormPolicyPort.listByApplicationFormId(any()))
            .willReturn(List.of());
        given(getFormResponseUseCase.findResponseWithAnswers(999L))
            .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> sut.getDetail(query))
            .isInstanceOf(ProjectDomainException.class)
            .hasFieldOrPropertyWithValue("baseCode", ProjectErrorCode.PROJECT_APPLICATION_NOT_FOUND);
    }

    @Test
    @DisplayName("getDetail_질문_fork_후에도_구_질문의_답변이_answersByQuestionId에_포함됨")
    void getDetail_fork된_구_질문_답변_노출() {
        // given
        // 질문 A(id=10)가 fork되어 비활성화됨. 지원자는 questionId=10에 답변을 제출한 상태.
        Project project = createProject(1L, "프로젝트A", null, 99L);
        ProjectMatchingRound round = createMatchingRound(
            7L, MatchingType.PLAN_DESIGN, MatchingPhase.FIRST);
        ProjectApplication application = createApplicationWithFormResponse(
            55L, project, round, 200L, ProjectApplicationStatus.SUBMITTED, 123L);

        GetProjectApplicationDetailQuery query = detailQuery(1L, 55L);
        given(loadProjectApplicationPort.findByIdWithDetails(55L))
            .willReturn(Optional.of(application));
        given(getChallengerUseCase.findByMemberIdAndGisuId(200L, GISU_ID))
            .willReturn(Optional.of(challengerInfoOf(200L, ChallengerPart.DESIGN)));
        given(loadProjectApplicationFormPolicyPort.listByApplicationFormId(any()))
            .willReturn(List.of());

        AnswerInfo forkedQuestionAnswer = AnswerInfo.builder()
            .id(501L)
            .formResponseId(123L)
            .questionId(10L)  // fork로 비활성화된 구 질문 ID
            .answeredAsType(QuestionType.SHORT_TEXT)
            .textValue("답변 내용")
            .selectedOptions(List.of())
            .fileIds(null)
            .times(null)
            .build();
        given(getFormResponseUseCase.findResponseWithAnswers(123L))
            .willReturn(Optional.of(FormResponseWithAnswersInfo.builder()
                .id(123L).formId(7L).respondentMemberId(200L)
                .status(FormResponseStatus.SUBMITTED)
                .answers(List.of(forkedQuestionAnswer))
                .build()));
        // answeredQuestionIds = {10L} 로 getFormWithStructureByQuestionIds 가 호출되어야 함
        given(getFormUseCase.getFormWithStructureByQuestionIds(eq(7L), eq(java.util.Set.of(10L))))
            .willReturn(FormWithStructureInfo.builder().formId(7L).sections(List.of()).build());

        // when
        ProjectApplicationDetailInfo result = sut.getDetail(query);

        // then
        assertThat(result.answersByQuestionId()).containsOnlyKeys(10L);
        assertThat(result.answersByQuestionId().get(10L).textValue()).isEqualTo("답변 내용");
        verify(getFormUseCase).getFormWithStructureByQuestionIds(eq(7L), eq(java.util.Set.of(10L)));
    }

    @Test
    @DisplayName("batchGetDetails_applicationId_목록을_batch_port와_batch_facade로_조회")
    void batchGetDetails_batch_조회() {
        // given
        Project project = createProject(1L, "프로젝트A", null, 99L);
        ProjectMatchingRound round = createMatchingRound(
            7L, MatchingType.PLAN_DESIGN, MatchingPhase.FIRST);
        ProjectApplication firstApplication = createApplicationWithFormResponse(
            55L, project, round, 200L, ProjectApplicationStatus.SUBMITTED, 123L);
        ProjectApplication secondApplication = createApplicationWithFormResponse(
            56L, project, round, 201L, ProjectApplicationStatus.SUBMITTED, 124L);

        given(loadProjectApplicationPort.batchGetByIdsWithDetails(Set.of(55L, 56L)))
            .willReturn(List.of(firstApplication, secondApplication));
        given(accessScopeResolver.resolveForProjectApplicantLists(eq(REQUESTER_ID), any()))
            .willReturn(Map.of(1L, new ProjectApplicationAccessScope.ProjectScoped(1L, true)));
        given(getChallengerUseCase.listByMemberIdsAndGisuId(Set.of(200L, 201L), GISU_ID))
            .willReturn(Map.of(
                200L, challengerInfoOf(200L, ChallengerPart.DESIGN),
                201L, challengerInfoOf(201L, ChallengerPart.WEB)
            ));
        given(loadProjectApplicationFormPolicyPort.listByApplicationFormIds(Set.of(33L)))
            .willReturn(Map.of(33L, List.of()));
        given(getFormResponseUseCase.findResponsesWithAnswers(Set.of(123L, 124L)))
            .willReturn(Map.of(
                123L, FormResponseWithAnswersInfo.builder()
                    .id(123L).formId(7L).respondentMemberId(200L)
                    .status(FormResponseStatus.SUBMITTED)
                    .answers(List.of())
                    .build(),
                124L, FormResponseWithAnswersInfo.builder()
                    .id(124L).formId(7L).respondentMemberId(201L)
                    .status(FormResponseStatus.SUBMITTED)
                    .answers(List.of())
                    .build()
            ));
        given(getFormUseCase.getFormWithStructureByQuestionIds(7L, Set.of()))
            .willReturn(FormWithStructureInfo.builder().formId(7L).sections(List.of()).build());

        // when
        Map<Long, ProjectApplicationDetailInfo> result = sut.batchGetDetails(List.of(
            detailQuery(1L, 55L),
            detailQuery(1L, 56L)
        ));

        // then
        assertThat(result).containsOnlyKeys(55L, 56L);
        assertThat(result.get(55L).applicantPart()).isEqualTo(ChallengerPart.DESIGN);
        assertThat(result.get(56L).applicantPart()).isEqualTo(ChallengerPart.WEB);
        verify(loadProjectApplicationPort, never()).findByIdWithDetails(any());
        verify(getFormResponseUseCase, never()).findResponseWithAnswers(any());
    }

    // ============================================================
    //                      Helper Methods
    // ============================================================

    private ProjectMatchingRound applicantListRound(Long id, MatchingPhase phase, Instant endsAt, Instant deadline) {
        ProjectMatchingRound round = createMatchingRound(id, MatchingType.PLAN_DEVELOPER, phase);
        ReflectionTestUtils.setField(round, "endsAt", endsAt);
        ReflectionTestUtils.setField(round, "decisionDeadline", deadline);
        return round;
    }

    private void givenApplicantListQueries(
        Project project, List<ProjectApplication> applications, SubjectAttributes subject, Instant now
    ) {
        given(clock.instant()).willReturn(now);
        given(checkPermissionUseCase.loadSubject(REQUESTER_ID)).willReturn(subject);
        given(loadProjectPort.getById(1L)).willReturn(project);
        given(loadProjectPort.listByIds(List.of(1L))).willReturn(List.of(project));
        given(accessScopeResolver.resolveForProjectApplicantList(REQUESTER_ID, project))
            .willReturn(new ProjectApplicationAccessScope.ProjectScoped(1L, true, false));
        given(accessScopeResolver.resolveForProjectApplicantLists(REQUESTER_ID, List.of(project)))
            .willReturn(Map.of(1L, new ProjectApplicationAccessScope.ProjectScoped(1L, true, false)));
        given(loadProjectApplicationPort.searchProjectApplications(eq(1L), isNull(), isNull(), eq(now), eq(true)))
            .willReturn(applications);
        given(loadProjectApplicationPort.searchProjectApplicationsByProjectIds(
            eq(Set.of(1L)), eq(Set.of(1L)), isNull(), isNull(), eq(now))).willReturn(applications);
    }

    private SearchProjectApplicationsQuery applicantListQuery(ProjectApplicationStatus status) {
        return SearchProjectApplicationsQuery.builder()
            .requesterMemberId(REQUESTER_ID).projectId(1L).status(status).build();
    }

    private SearchProjectApplicationsBatchQuery applicantListBatchQuery(ProjectApplicationStatus status) {
        return SearchProjectApplicationsBatchQuery.builder()
            .requesterMemberId(REQUESTER_ID).projectIds(List.of(1L)).status(status).build();
    }

    private GetMyProjectApplicationsQuery queryOf(ProjectApplicationStatus status) {
        return GetMyProjectApplicationsQuery.builder()
            .requesterMemberId(REQUESTER_ID)
            .gisuId(GISU_ID)
            .status(status)
            .build();
    }

    private ChallengerInfo challengerOf(ChallengerPart part) {
        return ChallengerInfo.builder()
            .challengerId(1L)
            .memberId(REQUESTER_ID)
            .gisuId(GISU_ID)
            .part(part)
            .build();
    }

    private ChallengerInfo challengerInfoOf(Long memberId, ChallengerPart part) {
        return ChallengerInfo.builder()
            .challengerId(memberId * 10)
            .memberId(memberId)
            .gisuId(GISU_ID)
            .part(part)
            .build();
    }

    private Project createProject(Long id, String name, String thumbnailFileId, Long ownerMemberId) {
        Project project = newInstance(Project.class);
        ReflectionTestUtils.setField(project, "id", id);
        ReflectionTestUtils.setField(project, "gisuId", GISU_ID);
        ReflectionTestUtils.setField(project, "chapterId", 1L);
        ReflectionTestUtils.setField(project, "status", ProjectStatus.IN_PROGRESS);
        ReflectionTestUtils.setField(project, "name", name);
        ReflectionTestUtils.setField(project, "thumbnailFileId", thumbnailFileId);
        ReflectionTestUtils.setField(project, "productOwnerMemberId", ownerMemberId);
        return project;
    }

    private ProjectMatchingRound createMatchingRound(Long id, MatchingType type, MatchingPhase phase) {
        // getDetail 의 조회 가능 시점 검증을 통과하도록 지원 종료(endsAt)를 과거로 둔다.
        ProjectMatchingRound round = newInstance(ProjectMatchingRound.class);
        ReflectionTestUtils.setField(round, "id", id);
        ReflectionTestUtils.setField(round, "type", type);
        ReflectionTestUtils.setField(round, "phase", phase);
        ReflectionTestUtils.setField(round, "endsAt", java.time.Instant.now().minusSeconds(3_600));
        ReflectionTestUtils.setField(round, "decisionDeadline", java.time.Instant.now().plusSeconds(3_600));
        return round;
    }

    private void markDecisionDeadlinePassed(ProjectMatchingRound round) {
        ReflectionTestUtils.setField(round, "decisionDeadline", java.time.Instant.now().minusSeconds(3_600));
    }

    private ProjectApplication createApplication(
        Long id, Project project, ProjectMatchingRound round, ProjectApplicationStatus status
    ) {
        ProjectApplicationForm form = newInstance(ProjectApplicationForm.class);
        ReflectionTestUtils.setField(form, "project", project);

        ProjectApplication application = newInstance(ProjectApplication.class);
        ReflectionTestUtils.setField(application, "id", id);
        ReflectionTestUtils.setField(application, "applicationForm", form);
        ReflectionTestUtils.setField(application, "appliedMatchingRound", round);
        ReflectionTestUtils.setField(application, "applicantMemberId", REQUESTER_ID);
        ReflectionTestUtils.setField(application, "status", status);
        return application;
    }

    private ProjectApplication createSubmittedApplication(
        Long id, Project project, ProjectMatchingRound round,
        Long applicantMemberId, ProjectApplicationStatus status
    ) {
        ProjectApplicationForm form = newInstance(ProjectApplicationForm.class);
        ReflectionTestUtils.setField(form, "project", project);

        ProjectApplication application = newInstance(ProjectApplication.class);
        ReflectionTestUtils.setField(application, "id", id);
        ReflectionTestUtils.setField(application, "applicationForm", form);
        ReflectionTestUtils.setField(application, "appliedMatchingRound", round);
        ReflectionTestUtils.setField(application, "applicantMemberId", applicantMemberId);
        ReflectionTestUtils.setField(application, "status", status);
        ReflectionTestUtils.setField(application, "submittedAt", java.time.Instant.parse("2026-04-22T01:30:00Z"));
        if (status == ProjectApplicationStatus.APPROVED || status == ProjectApplicationStatus.REJECTED) {
            ReflectionTestUtils.setField(application, "statusChangedAt",
                java.time.Instant.parse("2026-04-22T03:33:00Z"));
        }
        return application;
    }

    private GetProjectApplicationDetailQuery detailQuery(Long projectId, Long applicationId) {
        return detailQuery(projectId, applicationId, REQUESTER_ID);
    }

    private GetProjectApplicationDetailQuery detailQuery(Long projectId, Long applicationId, Long requesterMemberId) {
        return GetProjectApplicationDetailQuery.builder()
            .projectId(projectId)
            .applicationId(applicationId)
            .requesterMemberId(requesterMemberId)
            .build();
    }

    private ProjectApplication createApplicationWithFormResponse(
        Long id, Project project, ProjectMatchingRound round,
        Long applicantMemberId, ProjectApplicationStatus status, Long formResponseId
    ) {
        ProjectApplicationForm form = newInstance(ProjectApplicationForm.class);
        ReflectionTestUtils.setField(form, "id", 33L);
        ReflectionTestUtils.setField(form, "project", project);
        ReflectionTestUtils.setField(form, "formId", 7L);

        ProjectApplication application = newInstance(ProjectApplication.class);
        ReflectionTestUtils.setField(application, "id", id);
        ReflectionTestUtils.setField(application, "applicationForm", form);
        ReflectionTestUtils.setField(application, "appliedMatchingRound", round);
        ReflectionTestUtils.setField(application, "applicantMemberId", applicantMemberId);
        ReflectionTestUtils.setField(application, "status", status);
        ReflectionTestUtils.setField(application, "formResponseId", formResponseId);
        ReflectionTestUtils.setField(application, "submittedAt", java.time.Instant.parse("2026-04-22T01:30:00Z"));
        return application;
    }
}
