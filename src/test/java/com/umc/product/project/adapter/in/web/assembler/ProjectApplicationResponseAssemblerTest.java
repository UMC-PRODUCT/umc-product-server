package com.umc.product.project.adapter.in.web.assembler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.Instant;
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

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.MemberStatus;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.dto.MemberInfo;
import com.umc.product.project.adapter.in.web.dto.common.MatchingRoundPhaseView;
import com.umc.product.project.adapter.in.web.dto.response.MyProjectApplicationResponse;
import com.umc.product.project.adapter.in.web.dto.response.ProjectApplicantResponse;
import com.umc.product.project.adapter.in.web.dto.response.ProjectApplicationDetailResponse;
import com.umc.product.project.application.port.in.query.GetMyProjectApplicationsUseCase;
import com.umc.product.project.application.port.in.query.GetProjectApplicationDetailUseCase;
import com.umc.product.project.application.port.in.query.GetProjectMatchingRoundUseCase;
import com.umc.product.project.application.port.in.query.GetProjectUseCase;
import com.umc.product.project.application.port.in.query.GetRandomMatchedProjectMemberUseCase;
import com.umc.product.project.application.port.in.query.SearchProjectApplicationsUseCase;
import com.umc.product.project.application.port.in.query.dto.ApplicationFormInfo;
import com.umc.product.project.application.port.in.query.dto.GetMyProjectApplicationsQuery;
import com.umc.product.project.application.port.in.query.dto.GetProjectApplicationDetailQuery;
import com.umc.product.project.application.port.in.query.dto.ManagedProjectApplicationCardStatus;
import com.umc.product.project.application.port.in.query.dto.ProjectApplicationDetailInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectApplicationSummaryInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectApplicationViewStatus;
import com.umc.product.project.application.port.in.query.dto.ProjectInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectMatchingRoundInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectMemberInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectPartQuotaInfo;
import com.umc.product.project.application.port.in.query.dto.SearchProjectApplicationsQuery;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.enums.PartQuotaStatus;
import com.umc.product.project.domain.enums.ProjectApplicationStatus;
import com.umc.product.project.domain.enums.ProjectMemberStatus;
import com.umc.product.project.domain.enums.ProjectStatus;
import com.umc.product.survey.application.port.in.query.dto.FormResponseInfo;
import com.umc.product.survey.domain.enums.FormResponseStatus;

@ExtendWith(MockitoExtension.class)
class ProjectApplicationResponseAssemblerTest {

    private static final Long GISU_ID = 10L;

    @Mock
    GetMyProjectApplicationsUseCase getMyProjectApplicationsUseCase;
    @Mock
    GetRandomMatchedProjectMemberUseCase getRandomMatchedProjectMemberUseCase;
    @Mock
    SearchProjectApplicationsUseCase searchProjectApplicationsUseCase;
    @Mock
    GetProjectApplicationDetailUseCase getProjectApplicationDetailUseCase;
    @Mock
    GetProjectUseCase getProjectUseCase;
    @Mock
    GetProjectMatchingRoundUseCase getProjectMatchingRoundUseCase;
    @Mock
    GetMemberUseCase getMemberUseCase;
    @Mock
    GetChallengerUseCase getChallengerUseCase;

    @InjectMocks
    ProjectApplicationResponseAssembler sut;

    // ============================================================
    //          myApplicationsFor (본인 지원 내역 카드 합성) 테스트
    // ============================================================

    @Test
    @DisplayName("myApplicationsFor_PM_정보가_닉네임_실명_학교명을_포함해_응답에_조립된다")
    void PM_정보_조립() {
        // given
        GetMyProjectApplicationsQuery query = queryOf(null);
        ProjectApplicationSummaryInfo application = applicationSummaryOf(55L, 1L, 7L, 100L);
        ProjectInfo project = projectInfoOf(1L, "프로젝트A", 99L);
        ProjectMatchingRoundInfo round = roundInfoOf(7L, MatchingPhase.FIRST);

        given(getMyProjectApplicationsUseCase.listMyApplications(query))
            .willReturn(List.of(application));
        given(getRandomMatchedProjectMemberUseCase.findRandomMatched(query.requesterMemberId(), query.gisuId()))
            .willReturn(Optional.empty());
        given(getProjectUseCase.findAllByIds(any())).willReturn(Map.of(1L, project));
        given(getProjectMatchingRoundUseCase.findAllByIds(any())).willReturn(Map.of(7L, round));
        given(getMemberUseCase.findAllByIds(any()))
            .willReturn(Map.of(99L, memberOf(99L, "이방토", "이예원", "한양대 ERICA")));

        // when
        List<MyProjectApplicationResponse> result = sut.myApplicationsFor(query);

        // then
        assertThat(result).hasSize(1);
        MyProjectApplicationResponse response = result.get(0);
        assertThat(response.applicationId()).isEqualTo(55L);
        assertThat(response.projectId()).isEqualTo(1L);
        assertThat(response.project().productOwner().nickname()).isEqualTo("이방토");
        assertThat(response.project().productOwner().name()).isEqualTo("이예원");
        assertThat(response.project().productOwner().schoolName()).isEqualTo("한양대 ERICA");
        assertThat(response.matchingRound().id()).isEqualTo(7L);
        assertThat(response.matchingRound().type()).isEqualTo(MatchingType.PLAN_DEVELOPER);
        assertThat(response.matchingRound().phase()).isEqualTo(MatchingRoundPhaseView.FIRST);
        assertThat(response.status()).isEqualTo(ProjectApplicationViewStatus.SUBMITTED);
        assertThat(response.project().partQuotas())
            .extracting("part", "quota", "currentCount", "status")
            .containsExactly(
                org.assertj.core.groups.Tuple.tuple(
                    ChallengerPart.WEB, 3L, 1L, PartQuotaStatus.RECRUITING)
            );
    }

    @Test
    @DisplayName("myApplicationsFor_application도_randomMatched도_없으면_member_조회_없이_빈_리스트")
    void 빈_리스트_반환() {
        // given
        GetMyProjectApplicationsQuery query = queryOf(null);
        given(getMyProjectApplicationsUseCase.listMyApplications(query))
            .willReturn(List.of());
        given(getRandomMatchedProjectMemberUseCase.findRandomMatched(query.requesterMemberId(), query.gisuId()))
            .willReturn(Optional.empty());

        // when
        List<MyProjectApplicationResponse> result = sut.myApplicationsFor(query);

        // then
        assertThat(result).isEmpty();
        verify(getMemberUseCase, never()).findAllByIds(anySet());
    }

    @Test
    @DisplayName("myApplicationsFor_RANDOM_MATCHING_카드도_PM_닉네임_실명_학교명을_동일하게_합성한다")
    void RANDOM_MATCHING_카드_PM_정보_합성() {
        // given - application 0건, randomMatched 1건
        GetMyProjectApplicationsQuery query = queryOf(null);
        ProjectInfo project = projectInfoOf(2L, "프로젝트B", 88L);
        ProjectMemberInfo member = projectMemberInfoOf(401L, 2L, ChallengerPart.WEB);

        given(getMyProjectApplicationsUseCase.listMyApplications(query))
            .willReturn(List.of());
        given(getRandomMatchedProjectMemberUseCase.findRandomMatched(query.requesterMemberId(), query.gisuId()))
            .willReturn(Optional.of(member));
        given(getProjectUseCase.findAllByIds(any())).willReturn(Map.of(2L, project));
        given(getProjectMatchingRoundUseCase.findAllByIds(any())).willReturn(Map.of());
        given(getMemberUseCase.findAllByIds(any()))
            .willReturn(Map.of(88L, memberOf(88L, "랜덤피엠", "박서은", "이화여대")));

        // when
        List<MyProjectApplicationResponse> result = sut.myApplicationsFor(query);

        // then
        assertThat(result).hasSize(1);
        MyProjectApplicationResponse response = result.get(0);
        assertThat(response.applicationId()).isNull();
        assertThat(response.projectId()).isEqualTo(2L);
        assertThat(response.matchingRound().id()).isNull();
        assertThat(response.matchingRound().type()).isEqualTo(MatchingType.PLAN_DEVELOPER);
        assertThat(response.matchingRound().phase()).isEqualTo(MatchingRoundPhaseView.RANDOM_MATCHING);
        assertThat(response.status()).isEqualTo(ProjectApplicationViewStatus.APPROVED);
        assertThat(response.project().productOwner().nickname()).isEqualTo("랜덤피엠");
        assertThat(response.project().productOwner().name()).isEqualTo("박서은");
        assertThat(response.project().productOwner().schoolName()).isEqualTo("이화여대");
    }

    @Test
    @DisplayName("myApplicationsFor_application_2건_randomMatched_1건이면_RANDOM_MATCHING_카드는_마지막에_append")
    void 랜덤매칭_카드_끝에_append() {
        // given
        GetMyProjectApplicationsQuery query = queryOf(null);
        ProjectApplicationSummaryInfo appA = applicationSummaryOf(55L, 1L, 7L, 100L);
        ProjectApplicationSummaryInfo appB = applicationSummaryOf(56L, 2L, 8L, 100L);
        ProjectMemberInfo member = projectMemberInfoOf(401L, 3L, ChallengerPart.WEB);

        ProjectInfo pA = projectInfoOf(1L, "프로젝트A", 99L);
        ProjectInfo pB = projectInfoOf(2L, "프로젝트B", 99L);
        ProjectInfo pC = projectInfoOf(3L, "프로젝트C", 88L);

        given(getMyProjectApplicationsUseCase.listMyApplications(query))
            .willReturn(List.of(appA, appB));
        given(getRandomMatchedProjectMemberUseCase.findRandomMatched(query.requesterMemberId(), query.gisuId()))
            .willReturn(Optional.of(member));
        given(getProjectUseCase.findAllByIds(any()))
            .willReturn(Map.of(1L, pA, 2L, pB, 3L, pC));
        given(getProjectMatchingRoundUseCase.findAllByIds(any())).willReturn(Map.of(
            7L, roundInfoOf(7L, MatchingPhase.FIRST),
            8L, roundInfoOf(8L, MatchingPhase.SECOND)
        ));
        given(getMemberUseCase.findAllByIds(any())).willReturn(Map.of());

        // when
        List<MyProjectApplicationResponse> result = sut.myApplicationsFor(query);

        // then
        assertThat(result).hasSize(3);
        assertThat(result).extracting(MyProjectApplicationResponse::projectId)
            .containsExactly(1L, 2L, 3L);
        assertThat(result.get(0).matchingRound().phase()).isEqualTo(MatchingRoundPhaseView.FIRST);
        assertThat(result.get(1).matchingRound().phase()).isEqualTo(MatchingRoundPhaseView.SECOND);
        MyProjectApplicationResponse last = result.get(2);
        assertThat(last.applicationId()).isNull();
        assertThat(last.matchingRound().id()).isNull();
        assertThat(last.matchingRound().phase()).isEqualTo(MatchingRoundPhaseView.RANDOM_MATCHING);
        assertThat(last.status()).isEqualTo(ProjectApplicationViewStatus.APPROVED);
    }

    @Test
    @DisplayName("myApplicationsFor_status_명시_호출에서는_랜덤매칭_조회를_생략한다")
    void status_명시시_랜덤매칭_조회_생략() {
        // given
        GetMyProjectApplicationsQuery query = queryOf(ProjectApplicationStatus.SUBMITTED);
        given(getMyProjectApplicationsUseCase.listMyApplications(query))
            .willReturn(List.of());

        // when
        List<MyProjectApplicationResponse> result = sut.myApplicationsFor(query);

        // then
        assertThat(result).isEmpty();
        verify(getRandomMatchedProjectMemberUseCase, never())
            .findRandomMatched(any(), any());
    }

    @Test
    @DisplayName("myApplicationsFor_PM_정보_조회_결과가_없으면_productOwner는_null")
    void PM_정보_없음_null() {
        // given
        GetMyProjectApplicationsQuery query = queryOf(null);
        ProjectApplicationSummaryInfo application = applicationSummaryOf(55L, 1L, 7L, 100L);
        ProjectInfo project = projectInfoOf(1L, "프로젝트A", 99L);

        given(getMyProjectApplicationsUseCase.listMyApplications(query))
            .willReturn(List.of(application));
        given(getRandomMatchedProjectMemberUseCase.findRandomMatched(query.requesterMemberId(), query.gisuId()))
            .willReturn(Optional.empty());
        given(getProjectUseCase.findAllByIds(any())).willReturn(Map.of(1L, project));
        given(getProjectMatchingRoundUseCase.findAllByIds(any()))
            .willReturn(Map.of(7L, roundInfoOf(7L, MatchingPhase.FIRST)));
        given(getMemberUseCase.findAllByIds(any())).willReturn(Map.of());

        // when
        List<MyProjectApplicationResponse> result = sut.myApplicationsFor(query);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).project().productOwner()).isNull();
    }

    // ============================================================
    //          applicantsFor (PM/운영진 지원자 목록) 테스트
    // ============================================================

    @Test
    @DisplayName("applicantsFor_지원자_member_정보가_닉네임_실명_학교명을_포함해_응답에_조립된다")
    void applicantsFor_정상_조립() {
        // given
        SearchProjectApplicationsQuery query = SearchProjectApplicationsQuery.builder()
            .requesterMemberId(100L)
            .projectId(1L).build();
        ProjectApplicationSummaryInfo application = applicationSummaryOf(55L, 1L, 7L, 200L);
        ProjectInfo project = projectInfoOf(1L, "프로젝트A", 99L);

        given(searchProjectApplicationsUseCase.searchByProject(query))
            .willReturn(List.of(application));
        given(getProjectUseCase.getById(1L)).willReturn(project);
        given(getChallengerUseCase.batchGetByMemberIdsAndGisuId(eq(Set.of(200L)), eq(GISU_ID)))
            .willReturn(Map.of(200L, challengerInfoOf(200L, ChallengerPart.WEB)));
        given(getProjectMatchingRoundUseCase.findAllByIds(any()))
            .willReturn(Map.of(7L, roundInfoOf(7L, MatchingPhase.FIRST)));
        given(getMemberUseCase.findAllByIds(any()))
            .willReturn(Map.of(200L, memberOf(200L, "벨라", "황지원", "중앙대")));

        // when
        List<ProjectApplicantResponse> result = sut.applicantsFor(query);

        // then
        assertThat(result).hasSize(1);
        ProjectApplicantResponse response = result.get(0);
        assertThat(response.applicationId()).isEqualTo(55L);
        assertThat(response.applicant().memberId()).isEqualTo(200L);
        assertThat(response.applicant().nickname()).isEqualTo("벨라");
        assertThat(response.applicant().name()).isEqualTo("황지원");
        assertThat(response.applicant().schoolName()).isEqualTo("중앙대");
        assertThat(response.applicant().part()).isEqualTo(ChallengerPart.WEB);
        assertThat(response.matchingRound().id()).isEqualTo(7L);
        assertThat(response.matchingRound().type()).isEqualTo(MatchingType.PLAN_DEVELOPER);
        assertThat(response.matchingRound().phase()).isEqualTo(MatchingRoundPhaseView.FIRST);
        assertThat(response.status()).isEqualTo(ManagedProjectApplicationCardStatus.SUBMITTED);
    }

    @Test
    @DisplayName("applicantsFor_지원자가_없으면_member_조회_없이_빈_리스트")
    void applicantsFor_빈_리스트() {
        // given
        SearchProjectApplicationsQuery query = SearchProjectApplicationsQuery.builder()
            .requesterMemberId(100L)
            .projectId(1L).build();
        given(searchProjectApplicationsUseCase.searchByProject(query))
            .willReturn(List.of());

        // when
        List<ProjectApplicantResponse> result = sut.applicantsFor(query);

        // then
        assertThat(result).isEmpty();
        verify(getMemberUseCase, never()).findAllByIds(anySet());
        verify(getChallengerUseCase, never()).batchGetByMemberIdsAndGisuId(any(), any());
    }

    @Test
    @DisplayName("applicantsFor_member_조회_누락_시_applicant_의_닉네임_실명_학교명은_null")
    void applicantsFor_member_누락_시_null() {
        // given
        SearchProjectApplicationsQuery query = SearchProjectApplicationsQuery.builder()
            .requesterMemberId(100L)
            .projectId(1L).build();
        ProjectApplicationSummaryInfo application = applicationSummaryOf(55L, 1L, 7L, 200L);
        ProjectInfo project = projectInfoOf(1L, "프로젝트A", 99L);

        given(searchProjectApplicationsUseCase.searchByProject(query))
            .willReturn(List.of(application));
        given(getProjectUseCase.getById(1L)).willReturn(project);
        given(getChallengerUseCase.batchGetByMemberIdsAndGisuId(any(), eq(GISU_ID)))
            .willReturn(Map.of(200L, challengerInfoOf(200L, ChallengerPart.WEB)));
        given(getProjectMatchingRoundUseCase.findAllByIds(any()))
            .willReturn(Map.of(7L, roundInfoOf(7L, MatchingPhase.FIRST)));
        given(getMemberUseCase.findAllByIds(any())).willReturn(Map.of());

        // when
        List<ProjectApplicantResponse> result = sut.applicantsFor(query);

        // then
        assertThat(result).hasSize(1);
        ProjectApplicantResponse response = result.get(0);
        assertThat(response.applicant().memberId()).isEqualTo(200L);
        assertThat(response.applicant().nickname()).isNull();
        assertThat(response.applicant().name()).isNull();
        assertThat(response.applicant().schoolName()).isNull();
        // part 는 challenger 도메인 -> Assembler 가 batch 조회해 채운다 (member 누락과 무관)
        assertThat(response.applicant().part()).isEqualTo(ChallengerPart.WEB);
    }

    @Test
    @DisplayName("applicantsFor_part_필터를_지정하면_해당_파트_지원자만_반환")
    void applicantsFor_part_필터() {
        // given - WEB / ANDROID 지원자가 1명씩 있는데 query.part=WEB 으로 필터링
        SearchProjectApplicationsQuery query = SearchProjectApplicationsQuery.builder()
            .requesterMemberId(100L)
            .projectId(1L)
            .part(ChallengerPart.WEB)
            .build();
        ProjectApplicationSummaryInfo webApp = applicationSummaryOf(55L, 1L, 7L, 200L);
        ProjectApplicationSummaryInfo androidApp = applicationSummaryOf(56L, 1L, 7L, 201L);
        ProjectInfo project = projectInfoOf(1L, "프로젝트A", 99L);

        given(searchProjectApplicationsUseCase.searchByProject(query))
            .willReturn(List.of(webApp, androidApp));
        given(getProjectUseCase.getById(1L)).willReturn(project);
        given(getChallengerUseCase.batchGetByMemberIdsAndGisuId(any(), eq(GISU_ID)))
            .willReturn(Map.of(
                200L, challengerInfoOf(200L, ChallengerPart.WEB),
                201L, challengerInfoOf(201L, ChallengerPart.ANDROID)
            ));
        given(getProjectMatchingRoundUseCase.findAllByIds(any()))
            .willReturn(Map.of(7L, roundInfoOf(7L, MatchingPhase.FIRST)));
        given(getMemberUseCase.findAllByIds(any())).willReturn(Map.of());

        // when
        List<ProjectApplicantResponse> result = sut.applicantsFor(query);

        // then - WEB 1건만
        assertThat(result).hasSize(1);
        assertThat(result.get(0).applicant().memberId()).isEqualTo(200L);
        assertThat(result.get(0).applicant().part()).isEqualTo(ChallengerPart.WEB);
    }

    // ============================================================
    //                   detailFor (단건 상세) 테스트
    // ============================================================

    @Test
    @DisplayName("detailFor_지원자_member_정보가_닉네임_실명_학교명을_포함해_응답에_조립된다")
    void detailFor_정상_조립() {
        // given
        GetProjectApplicationDetailQuery query = detailQueryOf(1L, 55L);
        ProjectApplicationDetailInfo info = detailInfoOf(55L, 200L, ChallengerPart.DESIGN);

        given(getProjectApplicationDetailUseCase.getDetail(query)).willReturn(info);
        given(getMemberUseCase.findAllByIds(any()))
            .willReturn(Map.of(200L, memberOf(200L, "이방토", "이예원", "한양대 ERICA")));

        // when
        ProjectApplicationDetailResponse response = sut.detailFor(query);

        // then
        assertThat(response.applicationId()).isEqualTo(55L);
        assertThat(response.applicant().memberId()).isEqualTo(200L);
        assertThat(response.applicant().nickname()).isEqualTo("이방토");
        assertThat(response.applicant().name()).isEqualTo("이예원");
        assertThat(response.applicant().schoolName()).isEqualTo("한양대 ERICA");
        assertThat(response.applicant().part()).isEqualTo(ChallengerPart.DESIGN);
        assertThat(response.matchingRound().id()).isEqualTo(7L);
        assertThat(response.status()).isEqualTo(ProjectApplicationViewStatus.SUBMITTED);
        assertThat(response.formResponse().formResponseId()).isEqualTo(123L);
        assertThat(response.formResponse().sections()).isEmpty();
    }

    @Test
    @DisplayName("detailFor_member_조회_누락_시_applicant_의_닉네임_실명_학교명은_null")
    void detailFor_member_누락_시_null() {
        // given
        GetProjectApplicationDetailQuery query = detailQueryOf(1L, 55L);
        ProjectApplicationDetailInfo info = detailInfoOf(55L, 200L, ChallengerPart.DESIGN);

        given(getProjectApplicationDetailUseCase.getDetail(query)).willReturn(info);
        given(getMemberUseCase.findAllByIds(any())).willReturn(Map.of());

        // when
        ProjectApplicationDetailResponse response = sut.detailFor(query);

        // then
        assertThat(response.applicant().memberId()).isEqualTo(200L);
        assertThat(response.applicant().nickname()).isNull();
        assertThat(response.applicant().name()).isNull();
        assertThat(response.applicant().schoolName()).isNull();
        // part 는 challenger 도메인 -- Service 단에서 채워서 옴 (member 누락과 무관)
        assertThat(response.applicant().part()).isEqualTo(ChallengerPart.DESIGN);
    }

    // ============================================================
    //                      Helper Methods
    // ============================================================

    private GetMyProjectApplicationsQuery queryOf(ProjectApplicationStatus status) {
        return GetMyProjectApplicationsQuery.builder()
            .requesterMemberId(100L)
            .gisuId(GISU_ID)
            .status(status)
            .build();
    }

    private ProjectApplicationSummaryInfo applicationSummaryOf(
        Long applicationId, Long projectId, Long matchingRoundId, Long applicantMemberId
    ) {
        return ProjectApplicationSummaryInfo.builder()
            .id(applicationId)
            .applicantMemberId(applicantMemberId)
            .applicationFormId(33L)
            .projectId(projectId)
            .matchingRoundId(matchingRoundId)
            .status(ProjectApplicationStatus.SUBMITTED)
            .submittedAt(Instant.parse("2026-04-22T01:30:00Z"))
            .build();
    }

    private ProjectInfo projectInfoOf(Long projectId, String name, Long productOwnerMemberId) {
        return ProjectInfo.builder()
            .id(projectId)
            .status(ProjectStatus.IN_PROGRESS)
            .name(name)
            .gisuId(GISU_ID)
            .thumbnailImageUrl("https://cdn.example.com/thumb-" + projectId)
            .productOwnerMemberId(productOwnerMemberId)
            .coProductOwnerMemberIds(List.of())
            .partQuotas(List.of(ProjectPartQuotaInfo.of(ChallengerPart.WEB, 3L, 1L)))
            .build();
    }

    private ProjectMatchingRoundInfo roundInfoOf(Long id, MatchingPhase phase) {
        return ProjectMatchingRoundInfo.builder()
            .id(id)
            .type(MatchingType.PLAN_DEVELOPER)
            .phase(phase)
            .build();
    }

    private ProjectMemberInfo projectMemberInfoOf(Long memberId, Long projectId, ChallengerPart part) {
        return ProjectMemberInfo.builder()
            .projectMemberId(memberId)
            .projectId(projectId)
            .memberId(100L)
            .part(part)
            .isLeader(false)
            .status(ProjectMemberStatus.ACTIVE)
            .build();
    }

    private MemberInfo memberOf(Long id, String nickname, String name, String schoolName) {
        return MemberInfo.builder()
            .id(id)
            .nickname(nickname)
            .name(name)
            .schoolName(schoolName)
            .status(MemberStatus.ACTIVE)
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

    private GetProjectApplicationDetailQuery detailQueryOf(Long projectId, Long applicationId) {
        return GetProjectApplicationDetailQuery.builder()
            .projectId(projectId)
            .applicationId(applicationId)
            .requesterMemberId(100L)
            .build();
    }

    private ProjectApplicationDetailInfo detailInfoOf(
        Long applicationId, Long applicantMemberId, ChallengerPart part
    ) {
        ApplicationFormInfo emptyForm = ApplicationFormInfo.builder()
            .projectId(1L)
            .applicationFormId(33L)
            .title("지원폼")
            .description(null)
            .sections(List.of())
            .build();
        FormResponseInfo formResponse = FormResponseInfo.builder()
            .id(123L)
            .formId(7L)
            .respondentMemberId(applicantMemberId)
            .status(FormResponseStatus.SUBMITTED)
            .submittedAt(Instant.parse("2026-04-22T01:30:00Z"))
            .lastSavedAt(Instant.parse("2026-04-22T01:30:00Z"))
            .build();

        return ProjectApplicationDetailInfo.builder()
            .applicationId(applicationId)
            .applicantMemberId(applicantMemberId)
            .applicantPart(part)
            .matchingRoundId(7L)
            .matchingRoundType(MatchingType.PLAN_DESIGN)
            .matchingRoundPhase(MatchingPhase.FIRST)
            .status(ProjectApplicationViewStatus.SUBMITTED)
            .submittedAt(Instant.parse("2026-04-22T01:30:00Z"))
            .statusChangedAt(null)
            .formStructure(emptyForm)
            .formResponse(formResponse)
            .answersByQuestionId(Map.of())
            .filesByFileId(Map.of())
            .build();
    }
}
