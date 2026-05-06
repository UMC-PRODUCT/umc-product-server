package com.umc.product.project.application.service.query;

import static org.assertj.core.api.Assertions.assertThat;
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
import com.umc.product.project.application.port.in.query.dto.MyProjectApplicationCardInfo;
import com.umc.product.project.application.port.in.query.dto.MyProjectApplicationCardStatus;
import com.umc.product.project.application.port.out.LoadProjectApplicationPort;
import com.umc.product.project.application.port.out.LoadProjectMemberPort;
import com.umc.product.project.application.port.out.LoadProjectPartQuotaPort;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectApplication;
import com.umc.product.project.domain.ProjectApplicationForm;
import com.umc.product.project.domain.ProjectMatchingRound;
import com.umc.product.project.domain.ProjectPartQuota;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.enums.ProjectApplicationStatus;
import com.umc.product.project.domain.enums.ProjectStatus;
import com.umc.product.storage.application.port.in.query.GetFileUseCase;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
}
