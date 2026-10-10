package com.umc.product.member.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.umc.product.authorization.application.port.in.query.CheckChallengerAuthorityUseCase;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.member.application.port.in.query.CheckMemberExistenceUseCase;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.dto.MemberInfo;
import com.umc.product.member.application.port.in.query.dto.SearchMemberQuery;
import com.umc.product.member.domain.exception.MemberDomainException;
import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.organization.application.port.in.query.dto.chapter.ChapterInfo;
import com.umc.product.organization.application.port.in.query.dto.gisu.GisuInfo;

@ExtendWith(MockitoExtension.class)
class ChallengerManagementSearchPolicyTest {

    @Mock CheckMemberExistenceUseCase checkMemberExistenceUseCase;
    @Mock CheckChallengerAuthorityUseCase checkChallengerAuthorityUseCase;
    @Mock GetMemberUseCase getMemberUseCase;
    @Mock GetGisuUseCase getGisuUseCase;
    @Mock GetChapterUseCase getChapterUseCase;
    @InjectMocks ChallengerManagementSearchPolicy policy;

    @Test
    void 존재하지_않는_회원은_역할을_조회하기_전에_거부한다() {
        // given
        SearchMemberQuery query = query(11L, null);
        // when & then
        assertThatThrownBy(() -> policy.scope(query, 1L)).isInstanceOf(MemberDomainException.class);
        verifyNoInteractions(checkChallengerAuthorityUseCase, getMemberUseCase, getGisuUseCase, getChapterUseCase);
    }

    @Test
    void 최고_관리자는_챌린저_이력이나_활성_기수_없이_검색한다() {
        // given
        given(checkMemberExistenceUseCase.existsById(1L)).willReturn(true);
        given(checkChallengerAuthorityUseCase.isSuperAdmin(1L)).willReturn(true);
        given(getGisuUseCase.findActiveGisu()).willReturn(Optional.empty());
        // when
        SearchMemberQuery result = policy.scope(query(null, null), 1L);
        // then
        assertThat(result.gisuId()).isNull();
        assertThat(result.schoolId()).isNull();
        verifyNoInteractions(getMemberUseCase, getChapterUseCase);
    }

    @Test
    void 대상_기수_중앙_운영진은_요청한_학교를_검색한다() {
        // given
        given(checkMemberExistenceUseCase.existsById(1L)).willReturn(true);
        given(checkChallengerAuthorityUseCase.isCentralMemberInGisu(1L, 11L)).willReturn(true);
        SearchMemberQuery query = query(11L, 20L);
        // when & then
        assertThat(policy.scope(query, 1L)).isEqualTo(query);
    }

    @Test
    void 학교_회장단은_기수_생략시_활성_기수의_본인_학교로_제한한다() {
        // given
        given(checkMemberExistenceUseCase.existsById(1L)).willReturn(true);
        given(getGisuUseCase.findActiveGisu()).willReturn(Optional.of(new GisuInfo(11L, 11L, null, null, true)));
        given(getMemberUseCase.getById(1L)).willReturn(MemberInfo.builder().schoolId(10L).build());
        given(checkChallengerAuthorityUseCase.isSchoolCoreInGisu(1L, 11L, 10L)).willReturn(true);
        given(getChapterUseCase.findByGisuAndSchool(11L, 10L))
            .willReturn(Optional.of(new ChapterInfo(3L, "서울")));
        // when
        SearchMemberQuery result = policy.scope(query(null, null), 1L);
        // then
        assertThat(result).isEqualTo(query(11L, 10L));
    }

    @Test
    void 학교_회장단이_다른_학교_필터를_지정하면_거부한다() {
        // given
        given(checkMemberExistenceUseCase.existsById(1L)).willReturn(true);
        given(getMemberUseCase.getById(1L)).willReturn(MemberInfo.builder().schoolId(10L).build());
        given(checkChallengerAuthorityUseCase.isSchoolCoreInGisu(1L, 11L, 10L)).willReturn(true);
        // when & then
        assertThatThrownBy(() -> policy.scope(query(11L, 20L), 1L)).isInstanceOf(MemberDomainException.class);
    }

    @Test
    void 학교_회장단이_다른_지부_필터를_지정하면_거부한다() {
        // given
        given(checkMemberExistenceUseCase.existsById(1L)).willReturn(true);
        given(getMemberUseCase.getById(1L)).willReturn(MemberInfo.builder().schoolId(10L).build());
        given(checkChallengerAuthorityUseCase.isSchoolCoreInGisu(1L, 11L, 10L)).willReturn(true);
        given(getChapterUseCase.findByGisuAndSchool(11L, 10L))
            .willReturn(Optional.of(new ChapterInfo(4L, "경기")));
        // when & then
        assertThatThrownBy(() -> policy.scope(query(11L, 10L), 1L)).isInstanceOf(MemberDomainException.class);
    }

    @Test
    void 대상_기수의_중앙_운영진이나_학교_회장단이_아니면_거부한다() {
        // given
        given(checkMemberExistenceUseCase.existsById(1L)).willReturn(true);
        given(getMemberUseCase.getById(1L)).willReturn(MemberInfo.builder().schoolId(10L).build());
        // when & then
        assertThatThrownBy(() -> policy.scope(query(11L, null), 1L)).isInstanceOf(MemberDomainException.class);
    }

    @Test
    void 학교가_없는_비중앙_회원은_검색할_수_없다() {
        // given
        given(checkMemberExistenceUseCase.existsById(1L)).willReturn(true);
        given(getMemberUseCase.getById(1L)).willReturn(MemberInfo.builder().build());
        // when & then
        assertThatThrownBy(() -> policy.scope(query(11L, null), 1L)).isInstanceOf(MemberDomainException.class);
    }

    @Test
    void 일반_운영진은_검색_기수를_확정할_수_없으면_거부한다() {
        // given
        given(checkMemberExistenceUseCase.existsById(1L)).willReturn(true);
        given(getGisuUseCase.findActiveGisu()).willReturn(Optional.empty());
        // when & then
        assertThatThrownBy(() -> policy.scope(query(null, null), 1L)).isInstanceOf(MemberDomainException.class);
    }

    private SearchMemberQuery query(Long gisuId, Long schoolId) {
        return new SearchMemberQuery("검색어", gisuId, ChallengerPart.WEB, 3L, schoolId);
    }
}
