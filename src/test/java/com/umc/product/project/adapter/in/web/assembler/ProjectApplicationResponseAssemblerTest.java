package com.umc.product.project.adapter.in.web.assembler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.MemberStatus;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.dto.MemberInfo;
import com.umc.product.project.adapter.in.web.dto.response.MyProjectApplicationResponse;
import com.umc.product.project.adapter.in.web.dto.response.ProjectApplicantResponse;
import com.umc.product.project.adapter.in.web.dto.response.ProjectApplicationDetailResponse;
import com.umc.product.project.application.port.in.query.GetMyProjectApplicationsUseCase;
import com.umc.product.project.application.port.in.query.GetProjectApplicationDetailUseCase;
import com.umc.product.project.application.port.in.query.SearchProjectApplicationsUseCase;
import com.umc.product.project.application.port.in.query.dto.ApplicationFormInfo;
import com.umc.product.project.application.port.in.query.dto.GetMyProjectApplicationsQuery;
import com.umc.product.project.application.port.in.query.dto.GetProjectApplicationDetailQuery;
import com.umc.product.project.application.port.in.query.dto.ManagedProjectApplicationCardStatus;
import com.umc.product.project.application.port.in.query.dto.MatchingRoundPhaseView;
import com.umc.product.project.application.port.in.query.dto.MyProjectApplicationCardInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectApplicationCardInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectApplicationDetailInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectApplicationViewStatus;
import com.umc.product.project.application.port.in.query.dto.ProjectPartQuotaInfo;
import com.umc.product.project.application.port.in.query.dto.SearchProjectApplicationsQuery;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.enums.PartQuotaStatus;
import com.umc.product.survey.application.port.in.query.dto.FormResponseInfo;
import com.umc.product.survey.domain.enums.FormResponseStatus;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProjectApplicationResponseAssemblerTest {

    @Mock
    GetMyProjectApplicationsUseCase getMyProjectApplicationsUseCase;
    @Mock
    SearchProjectApplicationsUseCase searchProjectApplicationsUseCase;
    @Mock
    GetProjectApplicationDetailUseCase getProjectApplicationDetailUseCase;
    @Mock
    GetMemberUseCase getMemberUseCase;

    @InjectMocks
    ProjectApplicationResponseAssembler sut;

    @Test
    @DisplayName("myApplicationsFor_PM_정보가_닉네임_실명_학교명을_포함해_응답에_조립된다")
    void PM_정보_조립() {
        // given
        GetMyProjectApplicationsQuery query = queryOf();
        MyProjectApplicationCardInfo card = cardOf(55L, 1L, 99L);

        given(getMyProjectApplicationsUseCase.getMyApplications(query))
            .willReturn(List.of(card));
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
    @DisplayName("myApplicationsFor_지원_내역이_없으면_member_조회_없이_빈_리스트")
    void 빈_리스트_반환() {
        // given
        GetMyProjectApplicationsQuery query = queryOf();
        given(getMyProjectApplicationsUseCase.getMyApplications(query))
            .willReturn(List.of());

        // when
        List<MyProjectApplicationResponse> result = sut.myApplicationsFor(query);

        // then
        assertThat(result).isEmpty();
        verify(getMemberUseCase, never()).findAllByIds(anySet());
    }

    @Test
    @DisplayName("myApplicationsFor_RANDOM_MATCHING_카드도_PM_닉네임_실명_학교명을_동일하게_합성한다")
    void RANDOM_MATCHING_카드_PM_정보_합성() {
        // given - applicationId/matchingRoundId 가 null 인 카드도 회귀 없이 합성되는지
        GetMyProjectApplicationsQuery query = queryOf();
        MyProjectApplicationCardInfo card = randomMatchingCardOf(2L, 88L);

        given(getMyProjectApplicationsUseCase.getMyApplications(query))
            .willReturn(List.of(card));
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
    @DisplayName("myApplicationsFor_PM_정보_조회_결과가_없으면_productOwner는_null")
    void PM_정보_없음_null() {
        // given
        GetMyProjectApplicationsQuery query = queryOf();
        MyProjectApplicationCardInfo card = cardOf(55L, 1L, 99L);

        given(getMyProjectApplicationsUseCase.getMyApplications(query))
            .willReturn(List.of(card));
        given(getMemberUseCase.findAllByIds(any())).willReturn(Map.of());

        // when
        List<MyProjectApplicationResponse> result = sut.myApplicationsFor(query);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).project().productOwner()).isNull();
    }

    // ========== Helper Methods ==========

    private GetMyProjectApplicationsQuery queryOf() {
        return GetMyProjectApplicationsQuery.builder()
            .requesterMemberId(100L)
            .gisuId(10L)
            .status(null)
            .build();
    }

    private MyProjectApplicationCardInfo cardOf(Long applicationId, Long projectId, Long ownerMemberId) {
        return MyProjectApplicationCardInfo.builder()
            .applicationId(applicationId)
            .projectId(projectId)
            .projectName("프로젝트A")
            .projectThumbnailImageUrl("https://cdn.example.com/thumb-1")
            .productOwnerMemberId(ownerMemberId)
            .partQuotas(List.of(ProjectPartQuotaInfo.of(ChallengerPart.WEB, 3L, 1L)))
            .matchingRoundId(7L)
            .matchingRoundType(MatchingType.PLAN_DEVELOPER)
            .matchingRoundPhase(MatchingRoundPhaseView.FIRST)
            .status(ProjectApplicationViewStatus.SUBMITTED)
            .build();
    }

    private MyProjectApplicationCardInfo randomMatchingCardOf(Long projectId, Long ownerMemberId) {
        return MyProjectApplicationCardInfo.builder()
            .applicationId(null)
            .projectId(projectId)
            .projectName("프로젝트B")
            .projectThumbnailImageUrl(null)
            .productOwnerMemberId(ownerMemberId)
            .partQuotas(List.of())
            .matchingRoundId(null)
            .matchingRoundType(MatchingType.PLAN_DEVELOPER)
            .matchingRoundPhase(MatchingRoundPhaseView.RANDOM_MATCHING)
            .status(ProjectApplicationViewStatus.APPROVED)
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
        ProjectApplicationCardInfo card = applicantCardOf(55L, 200L, ChallengerPart.WEB);

        given(searchProjectApplicationsUseCase.searchByProject(query))
            .willReturn(List.of(card));
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
        assertThat(response.matchingRound().phase()).isEqualTo(MatchingPhase.FIRST);
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
    }

    @Test
    @DisplayName("applicantsFor_member_조회_누락_시_applicant_의_닉네임_실명_학교명은_null")
    void applicantsFor_member_누락_시_null() {
        // given
        SearchProjectApplicationsQuery query = SearchProjectApplicationsQuery.builder()
            .requesterMemberId(100L)
            .projectId(1L).build();
        ProjectApplicationCardInfo card = applicantCardOf(55L, 200L, ChallengerPart.WEB);

        given(searchProjectApplicationsUseCase.searchByProject(query))
            .willReturn(List.of(card));
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
        // part 는 challenger 도메인 -> Service 단에서 채워서 옴 (member 누락과 무관)
        assertThat(response.applicant().part()).isEqualTo(ChallengerPart.WEB);
    }

    private ProjectApplicationCardInfo applicantCardOf(
        Long applicationId, Long applicantMemberId, ChallengerPart part
    ) {
        return ProjectApplicationCardInfo.builder()
            .applicationId(applicationId)
            .applicantMemberId(applicantMemberId)
            .applicantPart(part)
            .matchingRoundId(7L)
            .matchingRoundType(MatchingType.PLAN_DEVELOPER)
            .matchingRoundPhase(MatchingPhase.FIRST)
            .status(ManagedProjectApplicationCardStatus.SUBMITTED)
            .submittedAt(Instant.parse("2026-04-22T01:30:00Z"))
            .statusChangedAt(null)
            .build();
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
