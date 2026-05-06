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
import com.umc.product.project.application.port.in.query.GetMyProjectApplicationsUseCase;
import com.umc.product.project.application.port.in.query.dto.GetMyProjectApplicationsQuery;
import com.umc.product.project.application.port.in.query.dto.MyProjectApplicationCardInfo;
import com.umc.product.project.application.port.in.query.dto.MyProjectApplicationCardStatus;
import com.umc.product.project.application.port.in.query.dto.ProjectPartQuotaInfo;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.enums.PartQuotaStatus;
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
        assertThat(response.matchingRound().phase()).isEqualTo(MatchingPhase.FIRST);
        assertThat(response.status()).isEqualTo(MyProjectApplicationCardStatus.SUBMITTED);
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
            .matchingRoundPhase(MatchingPhase.FIRST)
            .status(MyProjectApplicationCardStatus.SUBMITTED)
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
}
