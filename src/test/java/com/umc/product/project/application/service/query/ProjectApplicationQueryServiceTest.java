package com.umc.product.project.application.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.application.port.in.query.dto.GetMyProjectApplicationsQuery;
import com.umc.product.project.application.port.in.query.dto.ManagedProjectApplicationCardStatus;
import com.umc.product.project.application.port.in.query.dto.MyProjectApplicationCardInfo;
import com.umc.product.project.application.port.in.query.dto.MyProjectApplicationCardStatus;
import com.umc.product.project.application.port.in.query.dto.ProjectApplicationCardInfo;
import com.umc.product.project.application.port.in.query.dto.SearchProjectApplicationsQuery;
import com.umc.product.project.application.port.out.LoadProjectApplicationPort;
import com.umc.product.project.application.port.out.LoadProjectMemberPort;
import com.umc.product.project.application.port.out.LoadProjectPartQuotaPort;
import com.umc.product.project.application.port.out.LoadProjectPort;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectApplication;
import com.umc.product.project.domain.ProjectApplicationForm;
import com.umc.product.project.domain.ProjectMatchingRound;
import com.umc.product.project.domain.ProjectPartQuota;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.enums.ProjectApplicationStatus;
import com.umc.product.project.domain.enums.ProjectStatus;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.storage.application.port.in.query.GetFileUseCase;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ProjectApplicationQueryServiceTest {

    private static final Long REQUESTER_ID = 100L;
    private static final Long GISU_ID = 10L;
    @Mock
    LoadProjectApplicationPort loadProjectApplicationPort;
    @Mock
    LoadProjectPartQuotaPort loadProjectPartQuotaPort;
    @Mock
    LoadProjectMemberPort loadProjectMemberPort;
    @Mock
    LoadProjectPort loadProjectPort;
    @Mock
    GetChallengerUseCase getChallengerUseCase;
    @Mock
    GetFileUseCase getFileUseCase;
    @InjectMocks
    ProjectApplicationQueryService sut;

    private static <T> T newInstance(Class<T> clazz) {
        try {
            var constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("getMyApplications_해당_기수_챌린저가_아니면_빈_리스트_반환")
    void 챌린저_아님_빈_리스트() {
        // given
        GetMyProjectApplicationsQuery query = queryOf(null);
        given(getChallengerUseCase.findByMemberIdAndGisuId(REQUESTER_ID, GISU_ID))
            .willReturn(Optional.empty());

        // when
        List<MyProjectApplicationCardInfo> result = sut.getMyApplications(query);

        // then
        assertThat(result).isEmpty();
        verify(loadProjectApplicationPort, never())
            .searchMyApplications(any(), any(), any(), any());
    }

    @Test
    @DisplayName("getMyApplications_PLAN_파트는_지원_대상이_아니므로_빈_리스트")
    void PLAN_파트_빈_리스트() {
        // given
        GetMyProjectApplicationsQuery query = queryOf(null);
        given(getChallengerUseCase.findByMemberIdAndGisuId(REQUESTER_ID, GISU_ID))
            .willReturn(Optional.of(challengerOf(ChallengerPart.PLAN)));

        // when
        List<MyProjectApplicationCardInfo> result = sut.getMyApplications(query);

        // then
        assertThat(result).isEmpty();
        verify(loadProjectApplicationPort, never())
            .searchMyApplications(any(), any(), any(), any());
    }

    @Test
    @DisplayName("getMyApplications_ADMIN_파트는_지원_대상이_아니므로_빈_리스트")
    void ADMIN_파트_빈_리스트() {
        // given
        GetMyProjectApplicationsQuery query = queryOf(null);
        given(getChallengerUseCase.findByMemberIdAndGisuId(REQUESTER_ID, GISU_ID))
            .willReturn(Optional.of(challengerOf(ChallengerPart.ADMIN)));

        // when
        List<MyProjectApplicationCardInfo> result = sut.getMyApplications(query);

        // then
        assertThat(result).isEmpty();
        verify(loadProjectApplicationPort, never())
            .searchMyApplications(any(), any(), any(), any());
    }

    @Test
    @DisplayName("getMyApplications_DESIGN_파트는_PLAN_DESIGN_매칭으로_필터링")
    void DESIGN_PLAN_DESIGN_매칭() {
        // given
        GetMyProjectApplicationsQuery query = queryOf(null);
        given(getChallengerUseCase.findByMemberIdAndGisuId(REQUESTER_ID, GISU_ID))
            .willReturn(Optional.of(challengerOf(ChallengerPart.DESIGN)));
        given(loadProjectApplicationPort.searchMyApplications(
            REQUESTER_ID, GISU_ID, MatchingType.PLAN_DESIGN, null))
            .willReturn(List.of());

        // when
        sut.getMyApplications(query);

        // then
        verify(loadProjectApplicationPort)
            .searchMyApplications(REQUESTER_ID, GISU_ID, MatchingType.PLAN_DESIGN, null);
    }

    @Test
    @DisplayName("getMyApplications_WEB_파트는_PLAN_DEVELOPER_매칭으로_필터링")
    void WEB_PLAN_DEVELOPER_매칭() {
        // given
        GetMyProjectApplicationsQuery query = queryOf(null);
        given(getChallengerUseCase.findByMemberIdAndGisuId(REQUESTER_ID, GISU_ID))
            .willReturn(Optional.of(challengerOf(ChallengerPart.WEB)));
        given(loadProjectApplicationPort.searchMyApplications(
            REQUESTER_ID, GISU_ID, MatchingType.PLAN_DEVELOPER, null))
            .willReturn(List.of());

        // when
        sut.getMyApplications(query);

        // then
        verify(loadProjectApplicationPort)
            .searchMyApplications(REQUESTER_ID, GISU_ID, MatchingType.PLAN_DEVELOPER, null);
    }

    @Test
    @DisplayName("getMyApplications_status_명시시_해당_상태로_Port_호출")
    void status_명시_전달() {
        // given
        GetMyProjectApplicationsQuery query = queryOf(ProjectApplicationStatus.SUBMITTED);
        given(getChallengerUseCase.findByMemberIdAndGisuId(REQUESTER_ID, GISU_ID))
            .willReturn(Optional.of(challengerOf(ChallengerPart.WEB)));
        given(loadProjectApplicationPort.searchMyApplications(
            REQUESTER_ID, GISU_ID, MatchingType.PLAN_DEVELOPER, ProjectApplicationStatus.SUBMITTED))
            .willReturn(List.of());

        // when
        sut.getMyApplications(query);

        // then
        verify(loadProjectApplicationPort).searchMyApplications(
            REQUESTER_ID, GISU_ID, MatchingType.PLAN_DEVELOPER, ProjectApplicationStatus.SUBMITTED);
    }

    @Test
    @DisplayName("getMyApplications_지원_내역이_없으면_빈_리스트")
    void 지원_내역_없음_빈_리스트() {
        // given
        GetMyProjectApplicationsQuery query = queryOf(null);
        given(getChallengerUseCase.findByMemberIdAndGisuId(REQUESTER_ID, GISU_ID))
            .willReturn(Optional.of(challengerOf(ChallengerPart.WEB)));
        given(loadProjectApplicationPort.searchMyApplications(
            any(), any(), any(), any()))
            .willReturn(List.of());

        // when
        List<MyProjectApplicationCardInfo> result = sut.getMyApplications(query);

        // then
        assertThat(result).isEmpty();
        verify(loadProjectPartQuotaPort, never()).listByProjectIdsGroupedByProjectId(anyCollection());
    }

    @Test
    @DisplayName("getMyApplications_카드_조립_정상_프로젝트정보_partQuota_매칭라운드_썸네일_status_매핑")
    void 카드_조립_정상() {
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

        ProjectPartQuota webQuota = createPartQuota(project, ChallengerPart.WEB, 3L);
        given(loadProjectPartQuotaPort.listByProjectIdsGroupedByProjectId(any()))
            .willReturn(Map.of(1L, List.of(webQuota)));
        given(loadProjectMemberPort.countByProjectIdsGroupByProjectIdAndPart(any()))
            .willReturn(Map.of(1L, Map.of(ChallengerPart.WEB, 1L)));
        given(getFileUseCase.getFileLinks(anyList()))
            .willReturn(Map.of("thumb-1", "https://cdn.example.com/thumb-1"));

        // when
        List<MyProjectApplicationCardInfo> result = sut.getMyApplications(query);

        // then
        assertThat(result).hasSize(1);
        MyProjectApplicationCardInfo card = result.get(0);
        assertThat(card.applicationId()).isEqualTo(55L);
        assertThat(card.projectId()).isEqualTo(1L);
        assertThat(card.projectName()).isEqualTo("프로젝트A");
        assertThat(card.projectThumbnailImageUrl()).isEqualTo("https://cdn.example.com/thumb-1");
        assertThat(card.productOwnerMemberId()).isEqualTo(99L);
        assertThat(card.matchingRoundId()).isEqualTo(7L);
        assertThat(card.matchingRoundType()).isEqualTo(MatchingType.PLAN_DEVELOPER);
        assertThat(card.matchingRoundPhase()).isEqualTo(MatchingPhase.FIRST);
        assertThat(card.status()).isEqualTo(MyProjectApplicationCardStatus.SUBMITTED);
        assertThat(card.partQuotas())
            .extracting("part", "quota", "currentCount")
            .containsExactly(tuple(ChallengerPart.WEB, 3L, 1L));
    }

    // ========== Helper Methods ==========

    @Test
    @DisplayName("getMyApplications_썸네일_파일ID가_null이면_URL도_null")
    void 썸네일_없음_URL_null() {
        // given
        Project project = createProject(2L, "프로젝트B", null, 99L);
        ProjectMatchingRound round = createMatchingRound(
            8L, MatchingType.PLAN_DEVELOPER, MatchingPhase.SECOND);
        ProjectApplication application = createApplication(
            56L, project, round, ProjectApplicationStatus.PENDING);

        GetMyProjectApplicationsQuery query = queryOf(ProjectApplicationStatus.PENDING);
        given(getChallengerUseCase.findByMemberIdAndGisuId(REQUESTER_ID, GISU_ID))
            .willReturn(Optional.of(challengerOf(ChallengerPart.WEB)));
        given(loadProjectApplicationPort.searchMyApplications(
            any(), any(), any(), any()))
            .willReturn(List.of(application));
        given(loadProjectPartQuotaPort.listByProjectIdsGroupedByProjectId(any()))
            .willReturn(Map.of());
        given(loadProjectMemberPort.countByProjectIdsGroupByProjectIdAndPart(any()))
            .willReturn(Map.of());

        // when
        List<MyProjectApplicationCardInfo> result = sut.getMyApplications(query);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).projectThumbnailImageUrl()).isNull();
        assertThat(result.get(0).status()).isEqualTo(MyProjectApplicationCardStatus.PENDING);
        verify(getFileUseCase, never()).getFileLinks(anyList());
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
        ProjectMatchingRound round = newInstance(ProjectMatchingRound.class);
        ReflectionTestUtils.setField(round, "id", id);
        ReflectionTestUtils.setField(round, "type", type);
        ReflectionTestUtils.setField(round, "phase", phase);
        return round;
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

    private ProjectPartQuota createPartQuota(Project project, ChallengerPart part, long quota) {
        ProjectPartQuota pq = newInstance(ProjectPartQuota.class);
        ReflectionTestUtils.setField(pq, "project", project);
        ReflectionTestUtils.setField(pq, "part", part);
        ReflectionTestUtils.setField(pq, "quota", quota);
        return pq;
    }

    // ============================================================
    //          searchByProject (PM/운영진 지원자 목록 조회) 테스트
    // ============================================================

    @Test
    @DisplayName("searchByProject_PENDING_상태_필터를_사용하면_도메인_예외")
    void searchByProject_PENDING_필터_금지() {
        // given & when & then -- Query record compact constructor 에서 차단된다
        assertThatThrownBy(() -> SearchProjectApplicationsQuery.builder()
            .projectId(1L)
            .status(ProjectApplicationStatus.PENDING)
            .build())
            .isInstanceOf(ProjectDomainException.class);
    }

    @Test
    @DisplayName("searchByProject_지원서가_없으면_챌린저_조회_없이_빈_리스트")
    void searchByProject_빈_지원서_빈_리스트() {
        // given
        Project project = createProject(1L, "프로젝트A", null, 99L);
        SearchProjectApplicationsQuery query = SearchProjectApplicationsQuery.builder()
            .projectId(1L).build();

        given(loadProjectPort.getById(1L)).willReturn(project);
        given(loadProjectApplicationPort.searchProjectApplications(1L, null, null))
            .willReturn(List.of());

        // when
        List<ProjectApplicationCardInfo> result = sut.searchByProject(query);

        // then
        assertThat(result).isEmpty();
        verify(getChallengerUseCase, never())
            .batchGetByMemberIdsAndGisuId(any(), any());
    }

    @Test
    @DisplayName("searchByProject_지원자_정보_조립_정상_파트_상태_시각_매핑")
    void searchByProject_정상_조립() {
        // given
        Project project = createProject(1L, "프로젝트A", null, 99L);
        ProjectMatchingRound round = createMatchingRound(
            7L, MatchingType.PLAN_DEVELOPER, MatchingPhase.FIRST);
        ProjectApplication application = createSubmittedApplication(
            55L, project, round, 200L, ProjectApplicationStatus.APPROVED);

        SearchProjectApplicationsQuery query = SearchProjectApplicationsQuery.builder()
            .projectId(1L).build();

        given(loadProjectPort.getById(1L)).willReturn(project);
        given(loadProjectApplicationPort.searchProjectApplications(1L, null, null))
            .willReturn(List.of(application));
        given(getChallengerUseCase.batchGetByMemberIdsAndGisuId(eq(Set.of(200L)), eq(GISU_ID)))
            .willReturn(Map.of(200L, challengerInfoOf(200L, ChallengerPart.WEB)));

        // when
        List<ProjectApplicationCardInfo> result = sut.searchByProject(query);

        // then
        assertThat(result).hasSize(1);
        ProjectApplicationCardInfo card = result.get(0);
        assertThat(card.applicationId()).isEqualTo(55L);
        assertThat(card.applicantMemberId()).isEqualTo(200L);
        assertThat(card.applicantPart()).isEqualTo(ChallengerPart.WEB);
        assertThat(card.matchingRoundId()).isEqualTo(7L);
        assertThat(card.matchingRoundType()).isEqualTo(MatchingType.PLAN_DEVELOPER);
        assertThat(card.matchingRoundPhase()).isEqualTo(MatchingPhase.FIRST);
        assertThat(card.status()).isEqualTo(ManagedProjectApplicationCardStatus.APPROVED);
    }

    @Test
    @DisplayName("searchByProject_part_필터를_지정하면_해당_파트_지원자만_반환")
    void searchByProject_part_필터() {
        // given
        Project project = createProject(1L, "프로젝트A", null, 99L);
        ProjectMatchingRound round = createMatchingRound(
            7L, MatchingType.PLAN_DEVELOPER, MatchingPhase.FIRST);
        ProjectApplication webApp = createSubmittedApplication(
            55L, project, round, 200L, ProjectApplicationStatus.SUBMITTED);
        ProjectApplication androidApp = createSubmittedApplication(
            56L, project, round, 201L, ProjectApplicationStatus.SUBMITTED);

        SearchProjectApplicationsQuery query = SearchProjectApplicationsQuery.builder()
            .projectId(1L)
            .part(ChallengerPart.WEB)
            .build();

        given(loadProjectPort.getById(1L)).willReturn(project);
        given(loadProjectApplicationPort.searchProjectApplications(1L, null, null))
            .willReturn(List.of(webApp, androidApp));
        given(getChallengerUseCase.batchGetByMemberIdsAndGisuId(any(), eq(GISU_ID)))
            .willReturn(Map.of(
                200L, challengerInfoOf(200L, ChallengerPart.WEB),
                201L, challengerInfoOf(201L, ChallengerPart.ANDROID)
            ));

        // when
        List<ProjectApplicationCardInfo> result = sut.searchByProject(query);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).applicantMemberId()).isEqualTo(200L);
        assertThat(result.get(0).applicantPart()).isEqualTo(ChallengerPart.WEB);
    }

    @Test
    @DisplayName("searchByProject_matchingRoundId_status_필터는_repository_로_그대로_전달")
    void searchByProject_필터_전달() {
        // given
        Project project = createProject(1L, "프로젝트A", null, 99L);
        SearchProjectApplicationsQuery query = SearchProjectApplicationsQuery.builder()
            .projectId(1L)
            .matchingRoundId(7L)
            .status(ProjectApplicationStatus.APPROVED)
            .build();

        given(loadProjectPort.getById(1L)).willReturn(project);
        given(loadProjectApplicationPort.searchProjectApplications(
            1L, 7L, ProjectApplicationStatus.APPROVED))
            .willReturn(List.of());

        // when
        sut.searchByProject(query);

        // then
        verify(loadProjectApplicationPort).searchProjectApplications(
            1L, 7L, ProjectApplicationStatus.APPROVED);
    }

    private ChallengerInfo challengerInfoOf(Long memberId, ChallengerPart part) {
        return ChallengerInfo.builder()
            .challengerId(memberId * 10)
            .memberId(memberId)
            .gisuId(GISU_ID)
            .part(part)
            .build();
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
}
