package com.umc.product.notice.application.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.umc.product.authorization.application.port.in.query.ListChallengerRoleUseCase;
import com.umc.product.authorization.application.port.in.query.dto.ChallengerRoleInfo;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerBasicInfo;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.common.domain.enums.OrganizationType;
import com.umc.product.member.application.port.in.query.dto.MemberInfo;
import com.umc.product.notice.application.port.in.query.dto.NoticeAuthorInfo;
import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.organization.application.port.in.query.GetSchoolUseCase;
import com.umc.product.organization.application.port.in.query.dto.chapter.ChapterInfo;
import com.umc.product.organization.application.port.in.query.dto.gisu.GisuInfo;
import com.umc.product.organization.application.port.in.query.dto.school.SchoolDetailInfo;

@ExtendWith(MockitoExtension.class)
class NoticeAuthorQueryServiceTest {

    @Mock GetGisuUseCase getGisuUseCase;
    @Mock GetChallengerUseCase getChallengerUseCase;
    @Mock ListChallengerRoleUseCase listChallengerRoleUseCase;
    @Mock GetChapterUseCase getChapterUseCase;
    @Mock GetSchoolUseCase getSchoolUseCase;
    @InjectMocks NoticeAuthorQueryService sut;

    @Test
    void 활성_기수에서_최상위_직책을_일괄_표시한다() {
        // given: 같은 기수에 여러 중앙 역할이 있는 작성자와 역할 없는 작성자
        given(getGisuUseCase.findActiveGisu()).willReturn(Optional.of(
            new GisuInfo(10L, 10L, Instant.now(), Instant.now(), true)));
        Map<Long, MemberInfo> members = Map.of(
            1L, MemberInfo.builder().id(1L).name("작성자").nickname("닉네임").build(),
            2L, MemberInfo.builder().id(2L).name("일반회원").build());
        given(getChallengerUseCase.listBasicByMemberIdsAndGisuId(members.keySet(), 10L))
            .willReturn(List.of(new ChallengerBasicInfo(100L, 1L, 10L, null, false, null)));
        given(listChallengerRoleUseCase.listByChallengerIdsAndGisuId(Set.of(100L), 10L))
            .willReturn(List.of(role(ChallengerRoleType.CENTRAL_EDUCATION_TEAM_MEMBER),
                role(ChallengerRoleType.CENTRAL_VICE_PRESIDENT),
                ChallengerRoleInfo.builder().challengerId(100L).gisuId(10L)
                    .roleType(ChallengerRoleType.SCHOOL_VICE_PRESIDENT)
                    .organizationType(OrganizationType.SCHOOL).organizationId(5L).build()));

        // when
        Map<Long, NoticeAuthorInfo> result = sut.getAll(members);

        // then: 과거 기수 조회 없이 활성 기수의 최상위 역할을 표시
        assertThat(result.get(1L).roleType()).isEqualTo(ChallengerRoleType.CENTRAL_VICE_PRESIDENT);
        assertThat(result.get(1L).roleName()).isEqualTo("부총괄");
        assertThat(result.get(1L).organizationName()).isEqualTo("UMC 중앙");
        assertThat(result.get(1L).gisuId()).isEqualTo(10L);
        assertThat(result.get(2L).roleType()).isNull();
        assertThat(result.get(2L).name()).isEqualTo("일반회원");
        verify(listChallengerRoleUseCase).listByChallengerIdsAndGisuId(Set.of(100L), 10L);
        verifyNoInteractions(getChapterUseCase, getSchoolUseCase);
    }

    @Test
    void 학교와_지부의_ID가_같아도_작성자의_조직명을_구분한다() {
        // given: 학교 ID와 지부 ID는 서로 다른 종류의 식별자다.
        given(getGisuUseCase.findActiveGisu()).willReturn(Optional.of(
            new GisuInfo(10L, 10L, Instant.now(), Instant.now(), true)));
        Map<Long, MemberInfo> members = Map.of(
            1L, MemberInfo.builder().id(1L).name("회장").build(),
            2L, MemberInfo.builder().id(2L).name("지부장").build());
        given(getChallengerUseCase.listBasicByMemberIdsAndGisuId(members.keySet(), 10L)).willReturn(List.of(
            new ChallengerBasicInfo(100L, 1L, 10L, null, false, null),
            new ChallengerBasicInfo(200L, 2L, 10L, null, false, null)));
        given(listChallengerRoleUseCase.listByChallengerIdsAndGisuId(Set.of(100L, 200L), 10L)).willReturn(List.of(
            ChallengerRoleInfo.builder().challengerId(100L).gisuId(10L).roleType(ChallengerRoleType.SCHOOL_PRESIDENT)
                .organizationType(OrganizationType.SCHOOL).organizationId(5L).build(),
            ChallengerRoleInfo.builder().challengerId(200L).gisuId(10L).roleType(ChallengerRoleType.CHAPTER_PRESIDENT)
                .organizationType(OrganizationType.CHAPTER).organizationId(5L).build()));
        given(getSchoolUseCase.listDetailsByIds(Set.of(5L))).willReturn(List.of(
            new SchoolDetailInfo(null, null, "대상 학교", null, 5L, null, null, List.of(), true, null, null)));
        given(getChapterUseCase.listByGisuId(10L)).willReturn(List.of(new ChapterInfo(5L, "대상 지부")));

        // when
        Map<Long, NoticeAuthorInfo> result = sut.getAll(members);

        // then: challengerId가 아닌 memberId에 맞는 역할과 조직명을 반환한다.
        assertThat(result.get(1L).roleType()).isEqualTo(ChallengerRoleType.SCHOOL_PRESIDENT);
        assertThat(result.get(1L).organizationName()).isEqualTo("대상 학교");
        assertThat(result.get(2L).roleType()).isEqualTo(ChallengerRoleType.CHAPTER_PRESIDENT);
        assertThat(result.get(2L).organizationName()).isEqualTo("대상 지부");
        verify(getSchoolUseCase).listDetailsByIds(Set.of(5L));
        verify(getChapterUseCase).listByGisuId(10L);
    }

    @Test
    void 활성_기수가_없으면_회원_정보만_표시한다() {
        // given
        given(getGisuUseCase.findActiveGisu()).willReturn(Optional.empty());
        // when
        NoticeAuthorInfo result = sut.getAll(Map.of(1L,
            MemberInfo.builder().id(1L).name("작성자").build())).get(1L);
        // then
        assertThat(result.name()).isEqualTo("작성자");
        assertThat(result.gisuId()).isNull();
        assertThat(result.roleType()).isNull();
        verifyNoInteractions(getChallengerUseCase, listChallengerRoleUseCase, getChapterUseCase, getSchoolUseCase);
    }

    @Test
    void 빈_목록은_역할을_조회하지_않는다() {
        assertThat(sut.getAll(Map.of())).isEmpty();
        verifyNoInteractions(getGisuUseCase, getChallengerUseCase, listChallengerRoleUseCase);
    }

    private ChallengerRoleInfo role(ChallengerRoleType type) {
        return ChallengerRoleInfo.builder().challengerId(100L).gisuId(10L)
            .roleType(type).organizationType(OrganizationType.CENTRAL).build();
    }
}
