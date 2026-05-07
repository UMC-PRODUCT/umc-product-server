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
import com.umc.product.project.application.port.in.query.dto.GetProjectApplicationDetailQuery;
import com.umc.product.project.application.port.in.query.dto.ManagedProjectApplicationCardStatus;
import com.umc.product.project.application.port.in.query.dto.MatchingRoundPhaseView;
import com.umc.product.project.application.port.in.query.dto.MyProjectApplicationCardInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectApplicationCardInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectApplicationDetailInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectApplicationViewStatus;
import com.umc.product.project.application.port.in.query.dto.SearchProjectApplicationsQuery;
import com.umc.product.project.application.port.out.LoadProjectApplicationFormPolicyPort;
import com.umc.product.project.application.port.out.LoadProjectApplicationPort;
import com.umc.product.project.application.port.out.LoadProjectMemberPort;
import com.umc.product.project.application.port.out.LoadProjectPartQuotaPort;
import com.umc.product.project.application.port.out.LoadProjectPort;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectApplication;
import com.umc.product.project.domain.ProjectApplicationForm;
import com.umc.product.project.domain.ProjectMatchingRound;
import com.umc.product.project.domain.ProjectMember;
import com.umc.product.project.domain.ProjectPartQuota;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.enums.ProjectApplicationStatus;
import com.umc.product.project.domain.enums.ProjectMemberStatus;
import com.umc.product.project.domain.enums.ProjectStatus;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;
import com.umc.product.storage.application.port.in.query.GetFileUseCase;
import com.umc.product.storage.application.port.in.query.dto.FileInfo;
import com.umc.product.storage.domain.enums.FileCategory;
import com.umc.product.survey.application.port.in.query.GetFormResponseUseCase;
import com.umc.product.survey.application.port.in.query.GetFormUseCase;
import com.umc.product.survey.application.port.in.query.dto.AnswerInfo;
import com.umc.product.survey.application.port.in.query.dto.FormResponseWithAnswersInfo;
import com.umc.product.survey.application.port.in.query.dto.FormWithStructureInfo;
import com.umc.product.survey.domain.enums.FormResponseStatus;
import com.umc.product.survey.domain.enums.QuestionType;
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
    LoadProjectApplicationFormPolicyPort loadProjectApplicationFormPolicyPort;
    @Mock
    GetChallengerUseCase getChallengerUseCase;
    @Mock
    GetFileUseCase getFileUseCase;
    @Mock
    GetFormUseCase getFormUseCase;
    @Mock
    GetFormResponseUseCase getFormResponseUseCase;
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
        assertThat(card.matchingRoundPhase()).isEqualTo(MatchingRoundPhaseView.FIRST);
        assertThat(card.status()).isEqualTo(ProjectApplicationViewStatus.SUBMITTED);
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
        assertThat(result.get(0).status()).isEqualTo(ProjectApplicationViewStatus.PENDING);
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

    private ProjectMember createRandomMatchingMember(Long id, Project project, ChallengerPart part) {
        ProjectMember member = newInstance(ProjectMember.class);
        ReflectionTestUtils.setField(member, "id", id);
        ReflectionTestUtils.setField(member, "project", project);
        ReflectionTestUtils.setField(member, "memberId", REQUESTER_ID);
        ReflectionTestUtils.setField(member, "part", part);
        ReflectionTestUtils.setField(member, "status", ProjectMemberStatus.ACTIVE);
        // application=null 은 ManyToOne 미설정으로 자연스럽게 만들어진다.
        return member;
    }

    // ============================================================
    //   getMyApplications - 랜덤 매칭/운영진 강제 배정 카드 합성 (APPLY-004 확장)
    // ============================================================

    @Test
    @DisplayName("getMyApplications_application_0건_랜덤매칭_멤버_1건이면_RANDOM_MATCHING_카드_1건만_반환")
    void 랜덤매칭_단독_카드() {
        // given
        Project randomProject = createProject(2L, "프로젝트B", "thumb-2", 88L);
        ProjectMember member = createRandomMatchingMember(401L, randomProject, ChallengerPart.WEB);

        GetMyProjectApplicationsQuery query = queryOf(null);
        given(getChallengerUseCase.findByMemberIdAndGisuId(REQUESTER_ID, GISU_ID))
            .willReturn(Optional.of(challengerOf(ChallengerPart.WEB)));
        given(loadProjectApplicationPort.searchMyApplications(any(), any(), any(), any()))
            .willReturn(List.of());
        given(loadProjectMemberPort
            .findActiveWithoutApplicationByMemberIdAndGisuIdAndMatchingType(
                REQUESTER_ID, GISU_ID, MatchingType.PLAN_DEVELOPER))
            .willReturn(Optional.of(member));

        ProjectPartQuota webQuota = createPartQuota(randomProject, ChallengerPart.WEB, 3L);
        given(loadProjectPartQuotaPort.listByProjectIdsGroupedByProjectId(any()))
            .willReturn(Map.of(2L, List.of(webQuota)));
        given(loadProjectMemberPort.countByProjectIdsGroupByProjectIdAndPart(any()))
            .willReturn(Map.of(2L, Map.of(ChallengerPart.WEB, 1L)));
        given(getFileUseCase.getFileLinks(anyList()))
            .willReturn(Map.of("thumb-2", "https://cdn.example.com/thumb-2"));

        // when
        List<MyProjectApplicationCardInfo> result = sut.getMyApplications(query);

        // then
        assertThat(result).hasSize(1);
        MyProjectApplicationCardInfo card = result.get(0);
        assertThat(card.applicationId()).isNull();
        assertThat(card.projectId()).isEqualTo(2L);
        assertThat(card.projectName()).isEqualTo("프로젝트B");
        assertThat(card.projectThumbnailImageUrl()).isEqualTo("https://cdn.example.com/thumb-2");
        assertThat(card.productOwnerMemberId()).isEqualTo(88L);
        assertThat(card.matchingRoundId()).isNull();
        assertThat(card.matchingRoundType()).isEqualTo(MatchingType.PLAN_DEVELOPER);
        assertThat(card.matchingRoundPhase()).isEqualTo(MatchingRoundPhaseView.RANDOM_MATCHING);
        assertThat(card.status()).isEqualTo(ProjectApplicationViewStatus.APPROVED);
        assertThat(card.partQuotas())
            .extracting("part", "quota", "currentCount")
            .containsExactly(tuple(ChallengerPart.WEB, 3L, 1L));
    }

    @Test
    @DisplayName("getMyApplications_application_2건_랜덤매칭_멤버_1건이면_RANDOM_MATCHING_카드는_마지막에_append")
    void 랜덤매칭_카드_끝에_append() {
        // given - 서로 다른 프로젝트의 application 2건 + 또 다른 프로젝트의 랜덤 매칭 멤버 1건
        Project projectA = createProject(1L, "프로젝트A", null, 99L);
        Project projectB = createProject(2L, "프로젝트B", null, 99L);
        Project projectC = createProject(3L, "프로젝트C", null, 88L);

        ProjectMatchingRound roundFirst = createMatchingRound(7L, MatchingType.PLAN_DEVELOPER, MatchingPhase.FIRST);
        ProjectMatchingRound roundSecond = createMatchingRound(8L, MatchingType.PLAN_DEVELOPER, MatchingPhase.SECOND);
        ProjectApplication appA = createApplication(55L, projectA, roundFirst, ProjectApplicationStatus.REJECTED);
        ProjectApplication appB = createApplication(56L, projectB, roundSecond, ProjectApplicationStatus.SUBMITTED);

        ProjectMember member = createRandomMatchingMember(401L, projectC, ChallengerPart.WEB);

        GetMyProjectApplicationsQuery query = queryOf(null);
        given(getChallengerUseCase.findByMemberIdAndGisuId(REQUESTER_ID, GISU_ID))
            .willReturn(Optional.of(challengerOf(ChallengerPart.WEB)));
        // port 가 startsAt ASC -> updatedAt DESC 정렬 후 반환한다고 가정
        given(loadProjectApplicationPort.searchMyApplications(any(), any(), any(), any()))
            .willReturn(List.of(appA, appB));
        given(loadProjectMemberPort
            .findActiveWithoutApplicationByMemberIdAndGisuIdAndMatchingType(
                REQUESTER_ID, GISU_ID, MatchingType.PLAN_DEVELOPER))
            .willReturn(Optional.of(member));
        given(loadProjectPartQuotaPort.listByProjectIdsGroupedByProjectId(any()))
            .willReturn(Map.of());
        given(loadProjectMemberPort.countByProjectIdsGroupByProjectIdAndPart(any()))
            .willReturn(Map.of());

        // when
        List<MyProjectApplicationCardInfo> result = sut.getMyApplications(query);

        // then
        assertThat(result).hasSize(3);
        assertThat(result)
            .extracting(MyProjectApplicationCardInfo::projectId)
            .containsExactly(1L, 2L, 3L);
        assertThat(result.get(0).matchingRoundPhase()).isEqualTo(MatchingRoundPhaseView.FIRST);
        assertThat(result.get(1).matchingRoundPhase()).isEqualTo(MatchingRoundPhaseView.SECOND);
        // 마지막이 RANDOM_MATCHING 카드
        MyProjectApplicationCardInfo last = result.get(2);
        assertThat(last.applicationId()).isNull();
        assertThat(last.matchingRoundId()).isNull();
        assertThat(last.matchingRoundPhase()).isEqualTo(MatchingRoundPhaseView.RANDOM_MATCHING);
        assertThat(last.status()).isEqualTo(ProjectApplicationViewStatus.APPROVED);
    }

    @Test
    @DisplayName("getMyApplications_같은_프로젝트에_application_APPROVED와_ProjectMember가_둘다_있으면_application_카드만_노출")
    void 같은_프로젝트_중복_방지() {
        // given - 같은 프로젝트(id=1) 에 application APPROVED + ProjectMember 양쪽 존재.
        // port 가 application=null 만 반환하므로 단건 결과는 비어 있다 (랜덤 매칭 카드 미합성).
        Project project = createProject(1L, "프로젝트A", null, 99L);
        ProjectMatchingRound round = createMatchingRound(7L, MatchingType.PLAN_DEVELOPER, MatchingPhase.FIRST);
        ProjectApplication application = createApplication(55L, project, round, ProjectApplicationStatus.APPROVED);

        GetMyProjectApplicationsQuery query = queryOf(null);
        given(getChallengerUseCase.findByMemberIdAndGisuId(REQUESTER_ID, GISU_ID))
            .willReturn(Optional.of(challengerOf(ChallengerPart.WEB)));
        given(loadProjectApplicationPort.searchMyApplications(any(), any(), any(), any()))
            .willReturn(List.of(application));
        given(loadProjectMemberPort
            .findActiveWithoutApplicationByMemberIdAndGisuIdAndMatchingType(
                REQUESTER_ID, GISU_ID, MatchingType.PLAN_DEVELOPER))
            .willReturn(Optional.empty());
        given(loadProjectPartQuotaPort.listByProjectIdsGroupedByProjectId(any()))
            .willReturn(Map.of());
        given(loadProjectMemberPort.countByProjectIdsGroupByProjectIdAndPart(any()))
            .willReturn(Map.of());

        // when
        List<MyProjectApplicationCardInfo> result = sut.getMyApplications(query);

        // then -- application 카드 1건만
        assertThat(result).hasSize(1);
        assertThat(result.get(0).applicationId()).isEqualTo(55L);
        assertThat(result.get(0).matchingRoundPhase()).isEqualTo(MatchingRoundPhaseView.FIRST);
    }

    @Test
    @DisplayName("getMyApplications_DISMISSED_멤버는_port가_ACTIVE_필터링하므로_응답에서_제외된다")
    void DISMISSED_제외() {
        // given - port 계약상 DISMISSED 는 절대 반환되지 않으므로, port 가 empty 를 반환한다는 사실로 표현한다.
        GetMyProjectApplicationsQuery query = queryOf(null);
        given(getChallengerUseCase.findByMemberIdAndGisuId(REQUESTER_ID, GISU_ID))
            .willReturn(Optional.of(challengerOf(ChallengerPart.WEB)));
        given(loadProjectApplicationPort.searchMyApplications(any(), any(), any(), any()))
            .willReturn(List.of());
        given(loadProjectMemberPort
            .findActiveWithoutApplicationByMemberIdAndGisuIdAndMatchingType(
                REQUESTER_ID, GISU_ID, MatchingType.PLAN_DEVELOPER))
            .willReturn(Optional.empty());

        // when
        List<MyProjectApplicationCardInfo> result = sut.getMyApplications(query);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getMyApplications_status_명시_호출에서는_랜덤매칭_조회를_수행하지_않는다")
    void status_명시시_랜덤매칭_조회_생략() {
        // given
        GetMyProjectApplicationsQuery query = queryOf(ProjectApplicationStatus.SUBMITTED);
        given(getChallengerUseCase.findByMemberIdAndGisuId(REQUESTER_ID, GISU_ID))
            .willReturn(Optional.of(challengerOf(ChallengerPart.WEB)));
        given(loadProjectApplicationPort.searchMyApplications(any(), any(), any(), any()))
            .willReturn(List.of());

        // when
        List<MyProjectApplicationCardInfo> result = sut.getMyApplications(query);

        // then
        assertThat(result).isEmpty();
        verify(loadProjectMemberPort, never())
            .findActiveWithoutApplicationByMemberIdAndGisuIdAndMatchingType(any(), any(), any());
    }

    @Test
    @DisplayName("getMyApplications_application과_랜덤매칭_카드의_썸네일은_통합_projectId_집합으로_1회_batch")
    void 썸네일_통합_batch() {
        // given - application 의 thumb-x 와 랜덤 매칭 카드의 thumb-y 가 한 번에 batch 되는지 확인
        Project projectA = createProject(1L, "프로젝트A", "thumb-x", 99L);
        Project projectC = createProject(3L, "프로젝트C", "thumb-y", 88L);
        ProjectMatchingRound round = createMatchingRound(7L, MatchingType.PLAN_DEVELOPER, MatchingPhase.FIRST);
        ProjectApplication appA = createApplication(55L, projectA, round, ProjectApplicationStatus.SUBMITTED);
        ProjectMember member = createRandomMatchingMember(401L, projectC, ChallengerPart.WEB);

        GetMyProjectApplicationsQuery query = queryOf(null);
        given(getChallengerUseCase.findByMemberIdAndGisuId(REQUESTER_ID, GISU_ID))
            .willReturn(Optional.of(challengerOf(ChallengerPart.WEB)));
        given(loadProjectApplicationPort.searchMyApplications(any(), any(), any(), any()))
            .willReturn(List.of(appA));
        given(loadProjectMemberPort
            .findActiveWithoutApplicationByMemberIdAndGisuIdAndMatchingType(
                REQUESTER_ID, GISU_ID, MatchingType.PLAN_DEVELOPER))
            .willReturn(Optional.of(member));
        given(loadProjectPartQuotaPort.listByProjectIdsGroupedByProjectId(any()))
            .willReturn(Map.of());
        given(loadProjectMemberPort.countByProjectIdsGroupByProjectIdAndPart(any()))
            .willReturn(Map.of());
        given(getFileUseCase.getFileLinks(anyList()))
            .willReturn(Map.of(
                "thumb-x", "https://cdn/thumb-x",
                "thumb-y", "https://cdn/thumb-y"
            ));

        // when
        List<MyProjectApplicationCardInfo> result = sut.getMyApplications(query);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).projectThumbnailImageUrl()).isEqualTo("https://cdn/thumb-x");
        assertThat(result.get(1).projectThumbnailImageUrl()).isEqualTo("https://cdn/thumb-y");
        // 통합 batch -- getFileLinks 가 정확히 1회 호출
        verify(getFileUseCase, org.mockito.Mockito.times(1)).getFileLinks(anyList());
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
        verify(getFormUseCase, never()).getFormWithStructure(any());
    }

    @Test
    @DisplayName("getDetail_정상_조립_applicantPart_status_매핑_및_answers_files_매핑_확인")
    void getDetail_정상_조립() {
        // given
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

        FormWithStructureInfo formStructure = FormWithStructureInfo.builder()
            .formId(7L)
            .title("지원폼")
            .description(null)
            .sections(List.of())
            .build();
        given(getFormUseCase.getFormWithStructure(7L)).willReturn(formStructure);
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
        assertThat(result.status()).isEqualTo(ProjectApplicationViewStatus.SUBMITTED);
        assertThat(result.formResponse().id()).isEqualTo(123L);
        assertThat(result.answersByQuestionId()).containsOnlyKeys(11L, 16L);
        assertThat(result.filesByFileId()).containsKeys("f-abc");
        assertThat(result.filesByFileId().get("f-abc").originalFileName()).isEqualTo("포트폴리오.pdf");
    }

    @Test
    @DisplayName("getDetail_PENDING_상태인데_지원자_본인이_아니면_NOT_FOUND_위장")
    void getDetail_PENDING_타인_차단() {
        // given - 지원자 본인은 200L, 호출자는 300L (다른 챌린저/운영진)
        Project project = createProject(1L, "프로젝트A", null, 99L);
        ProjectMatchingRound round = createMatchingRound(
            7L, MatchingType.PLAN_DESIGN, MatchingPhase.FIRST);
        ProjectApplication application = createApplicationWithFormResponse(
            55L, project, round, 200L, ProjectApplicationStatus.PENDING, 123L);

        GetProjectApplicationDetailQuery query = detailQuery(1L, 55L, 300L);
        given(loadProjectApplicationPort.findByIdWithDetails(55L))
            .willReturn(Optional.of(application));

        // when & then
        assertThatThrownBy(() -> sut.getDetail(query))
            .isInstanceOf(ProjectDomainException.class)
            .hasFieldOrPropertyWithValue("baseCode", ProjectErrorCode.PROJECT_APPLICATION_NOT_FOUND);
        verify(getChallengerUseCase, never()).findByMemberIdAndGisuId(any(), any());
    }

    @Test
    @DisplayName("getDetail_PENDING_상태이지만_지원자_본인_호출이면_정상_조회")
    void getDetail_PENDING_본인_허용() {
        // given - applicantMemberId 와 requesterMemberId 가 동일
        Project project = createProject(1L, "프로젝트A", null, 99L);
        ProjectMatchingRound round = createMatchingRound(
            7L, MatchingType.PLAN_DESIGN, MatchingPhase.FIRST);
        ProjectApplication application = createApplicationWithFormResponse(
            55L, project, round, 200L, ProjectApplicationStatus.PENDING, 123L);

        GetProjectApplicationDetailQuery query = detailQuery(1L, 55L, 200L);
        given(loadProjectApplicationPort.findByIdWithDetails(55L))
            .willReturn(Optional.of(application));
        given(getChallengerUseCase.findByMemberIdAndGisuId(200L, GISU_ID))
            .willReturn(Optional.of(challengerInfoOf(200L, ChallengerPart.DESIGN)));
        given(getFormUseCase.getFormWithStructure(7L)).willReturn(
            FormWithStructureInfo.builder().formId(7L).sections(List.of()).build());
        given(loadProjectApplicationFormPolicyPort.listByApplicationFormId(any()))
            .willReturn(List.of());
        given(getFormResponseUseCase.findResponseWithAnswers(123L))
            .willReturn(Optional.of(FormResponseWithAnswersInfo.builder()
                .id(123L).formId(7L).respondentMemberId(200L)
                .status(FormResponseStatus.DRAFT)
                .answers(List.of())
                .build()));

        // when
        ProjectApplicationDetailInfo result = sut.getDetail(query);

        // then
        assertThat(result.applicationId()).isEqualTo(55L);
        assertThat(result.status()).isEqualTo(ProjectApplicationViewStatus.PENDING);
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
        given(getFormUseCase.getFormWithStructure(7L)).willReturn(
            FormWithStructureInfo.builder().formId(7L).sections(List.of()).build());
        given(loadProjectApplicationFormPolicyPort.listByApplicationFormId(any()))
            .willReturn(List.of());
        given(getFormResponseUseCase.findResponseWithAnswers(999L))
            .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> sut.getDetail(query))
            .isInstanceOf(ProjectDomainException.class)
            .hasFieldOrPropertyWithValue("baseCode", ProjectErrorCode.PROJECT_APPLICATION_NOT_FOUND);
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
