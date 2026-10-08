package com.umc.product.notice.application.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.LongStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.umc.product.authorization.application.port.in.query.ListChallengerRoleUseCase;
import com.umc.product.authorization.application.port.in.query.dto.ChallengerRoleInfo;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerBasicInfo;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.dto.MemberInfo;
import com.umc.product.notice.domain.NoticeTargetInfo;
import com.umc.product.notice.domain.enums.NoticeTab;
import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import com.umc.product.organization.application.port.in.query.dto.chapter.ChapterInfo;

@ExtendWith(MockitoExtension.class)
class NoticeAudienceResolverTest {

    @Mock GetChallengerUseCase getChallengerUseCase;
    @Mock ListChallengerRoleUseCase listChallengerRoleUseCase;
    @Mock GetMemberUseCase getMemberUseCase;
    @Mock GetChapterUseCase getChapterUseCase;
    @InjectMocks NoticeAudienceResolver sut;

    @Test
    void 전체_기수_학교_공지의_회원은_중복없이_선정한다() {
        // given: 기수 중복 이력과 다른 학교 회원, 학교 없는 회원
        given(getChallengerUseCase.getAllLatestGisuPerMemberWithoutChallengerPoints()).willReturn(List.of(
            challenger(1L, 9L), challenger(1L, 10L), challenger(2L, 10L), challenger(3L, 10L)));
        given(getMemberUseCase.findAllByIds(Set.of(1L, 2L, 3L))).willReturn(Map.of(
            1L, member(1L, 5L), 2L, member(2L, 6L), 3L, member(3L, null)));
        // when
        List<Long> result = sut.resolve(new NoticeTargetInfo(null, null, 5L, List.of(), NoticeTab.CHALLENGER));
        // then
        assertThat(result).containsExactly(1L);
        verifyNoInteractions(listChallengerRoleUseCase, getChapterUseCase);
    }

    @Test
    void 지부와_파트가_모두_맞는_챌린저만_선정한다() {
        // given
        given(getChallengerUseCase.getAllByGisuId(10L)).willReturn(List.of(challenger(1L, 10L), challenger(2L, 10L)));
        given(getMemberUseCase.findAllByIds(Set.of(1L, 2L))).willReturn(Map.of(
            1L, member(1L, 5L), 2L, member(2L, 6L)));
        given(getChapterUseCase.getChapterMapByGisuIdsAndSchoolIds(Set.of(10L), Set.of(5L, 6L)))
            .willReturn(Map.of(10L, Map.of(5L, new ChapterInfo(3L, "대상 지부"),
                6L, new ChapterInfo(4L, "다른 지부"))));
        // when
        List<Long> result = sut.resolve(new NoticeTargetInfo(10L, 3L, null,
            List.of(ChallengerPart.WEB_PRODUCT_ENGINEER), NoticeTab.CHALLENGER));
        // then
        assertThat(result).containsExactly(1L);
    }

    @Test
    void 학교_운영진_알림에서_일반_챌린저와_다른_학교_파트_지부장을_제외한다() {
        // given: 7번은 일반 챌린저, 지부장 조직 ID는 대상 학교 ID와 동일
        given(getChallengerUseCase.listBasicByGisuId(10L)).willReturn(LongStream.rangeClosed(1, 7)
            .mapToObj(id -> new ChallengerBasicInfo(id, id, 10L, null, false, null)).toList());
        given(listChallengerRoleUseCase.listByChallengerIdsAndGisuId(Set.of(1L, 2L, 3L, 4L, 5L, 6L, 7L), 10L))
            .willReturn(List.of(
                role(1L, ChallengerRoleType.CENTRAL_OPERATING_TEAM_MEMBER, null, null),
                role(2L, ChallengerRoleType.SCHOOL_PRESIDENT, 5L, null),
                role(2L, ChallengerRoleType.SCHOOL_PART_LEADER, 5L, ChallengerPart.WEB_PRODUCT_ENGINEER),
                role(3L, ChallengerRoleType.SCHOOL_PART_LEADER, 5L, ChallengerPart.WEB_PRODUCT_ENGINEER),
                role(4L, ChallengerRoleType.SCHOOL_PART_LEADER, 5L, ChallengerPart.MOBILE_PRODUCT_ENGINEER),
                role(5L, ChallengerRoleType.SCHOOL_PRESIDENT, 6L, null),
                role(6L, ChallengerRoleType.CHAPTER_PRESIDENT, 5L, null)));
        // when
        List<Long> result = sut.resolve(new NoticeTargetInfo(10L, null, 5L,
            List.of(ChallengerPart.WEB_PRODUCT_ENGINEER), NoticeTab.SCHOOL_PART_LEADER));
        // then
        assertThat(result).containsExactlyInAnyOrder(1L, 2L, 3L).doesNotHaveDuplicates();
        verifyNoInteractions(getMemberUseCase, getChapterUseCase);
    }

    @Test
    void 중앙_발신_회장단_알림에_지부장을_포함하고_파트장은_제외한다() {
        // given
        given(getChallengerUseCase.listBasicByGisuId(10L)).willReturn(List.of(
            new ChallengerBasicInfo(1L, 1L, 10L, null, false, null),
            new ChallengerBasicInfo(2L, 2L, 10L, null, false, null)));
        given(listChallengerRoleUseCase.listByChallengerIdsAndGisuId(Set.of(1L, 2L), 10L)).willReturn(List.of(
            role(1L, ChallengerRoleType.CHAPTER_PRESIDENT, 3L, null),
            role(2L, ChallengerRoleType.SCHOOL_PART_LEADER, 5L, ChallengerPart.WEB_PRODUCT_ENGINEER)));
        // when
        List<Long> result = sut.resolve(new NoticeTargetInfo(10L, null, null, List.of(), NoticeTab.SCHOOL_CORE));
        // then
        assertThat(result).containsExactly(1L);
    }

    private ChallengerInfo challenger(Long memberId, Long gisuId) {
        return ChallengerInfo.builder().memberId(memberId).gisuId(gisuId)
            .part(ChallengerPart.WEB_PRODUCT_ENGINEER).build();
    }

    private MemberInfo member(Long id, Long schoolId) {
        return MemberInfo.builder().id(id).schoolId(schoolId).build();
    }

    private ChallengerRoleInfo role(Long challengerId, ChallengerRoleType type, Long organizationId,
                                   ChallengerPart part) {
        return ChallengerRoleInfo.builder().challengerId(challengerId).gisuId(10L).roleType(type)
            .organizationId(organizationId).organizationType(type.organizationType()).responsiblePart(part).build();
    }
}
