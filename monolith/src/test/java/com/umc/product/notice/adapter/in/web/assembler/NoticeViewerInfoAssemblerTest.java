package com.umc.product.notice.adapter.in.web.assembler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

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

import com.umc.product.authorization.application.port.in.query.GetChallengerRoleUseCase;
import com.umc.product.authorization.application.port.in.query.dto.ChallengerRoleInfo;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.common.domain.enums.OrganizationType;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.dto.MemberInfo;
import com.umc.product.notice.application.port.in.query.dto.NoticeViewerInfo;
import com.umc.product.notice.domain.enums.NoticeTab;
import com.umc.product.notice.domain.exception.NoticeDomainException;
import com.umc.product.notice.domain.exception.NoticeErrorCode;
import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.organization.application.port.in.query.dto.chapter.ChapterInfo;
import com.umc.product.organization.application.port.in.query.dto.chapter.ChapterWithSchoolsInfo;
import com.umc.product.organization.application.port.in.query.dto.gisu.GisuInfo;

@ExtendWith(MockitoExtension.class)
@DisplayName("NoticeViewerInfoAssembler")
class NoticeViewerInfoAssemblerTest {

    private static final Long MEMBER_ID = 1L;
    private static final Long GISU_ID = 9L;

    @Mock
    GetChallengerUseCase getChallengerUseCase;

    @Mock
    GetChallengerRoleUseCase getChallengerRoleUseCase;

    @Mock
    GetMemberUseCase getMemberUseCase;

    @Mock
    GetChapterUseCase getChapterUseCase;

    @Mock
    GetGisuUseCase getGisuUseCase;

    @InjectMocks
    NoticeViewerInfoAssembler sut;

    @Test
    @DisplayName("system role SUPER_ADMIN은 challenger role 없이 최상위 공지 역할을 얻는다")
    void system_super_admin_has_central_member_notice_role() {
        given(getChallengerUseCase.findByMemberIdAndGisuId(MEMBER_ID, GISU_ID)).willReturn(Optional.empty());
        given(getChallengerRoleUseCase.isSuperAdmin(MEMBER_ID)).willReturn(true);
        given(getMemberUseCase.findAllByIds(java.util.Set.of(MEMBER_ID))).willReturn(Map.of(
            MEMBER_ID,
            MemberInfo.builder().id(MEMBER_ID).build()
        ));

        NoticeViewerInfo result = sut.toMemberIdAndGisuId(MEMBER_ID, GISU_ID, null);

        assertThat(result.viewerRole()).isEqualTo(NoticeTab.CENTRAL_MEMBER);
        verifyNoInteractions(getChapterUseCase);
    }

    @Test
    @DisplayName("수강 없는 운영진(part=null)은 memberParts에 null이 섞이지 않는다")
    void 운영진_null_파트_방어() {
        given(getChallengerUseCase.findByMemberIdAndGisuId(MEMBER_ID, GISU_ID)).willReturn(
            Optional.of(ChallengerInfo.builder()
                .memberId(MEMBER_ID)
                .gisuId(GISU_ID)
                .part(null)
                .build())
        );
        given(getChallengerRoleUseCase.getAllResponsiblePartByMemberIdAndGisuId(MEMBER_ID, GISU_ID))
            .willReturn(Set.of());
        given(getChallengerRoleUseCase.isSuperAdmin(MEMBER_ID)).willReturn(false);
        given(getChallengerRoleUseCase.findAllByMemberId(MEMBER_ID)).willReturn(List.of());
        given(getMemberUseCase.findAllByIds(Set.of(MEMBER_ID))).willReturn(Map.of(
            MEMBER_ID,
            MemberInfo.builder().id(MEMBER_ID).build()
        ));

        NoticeViewerInfo result = sut.toMemberIdAndGisuId(MEMBER_ID, GISU_ID, null);

        assertThat(result.memberParts()).doesNotContainNull().isEmpty();
    }

    @Test
    void 지부장은_담당_지부와_소속_학교_범위를_사용한다() {
        // given: 과거 기수 중앙 역할은 현재 조회 권한에 사용하지 않는다.
        given(getChallengerRoleUseCase.findAllByMemberId(MEMBER_ID)).willReturn(List.of(
            ChallengerRoleInfo.builder().gisuId(8L).roleType(ChallengerRoleType.CENTRAL_PRESIDENT).build(),
            ChallengerRoleInfo.builder().gisuId(GISU_ID).roleType(ChallengerRoleType.CHAPTER_PRESIDENT)
                .organizationType(OrganizationType.CHAPTER).organizationId(3L).build()));
        given(getChapterUseCase.getChaptersWithSchoolsByGisuId(GISU_ID)).willReturn(List.of(
            new ChapterWithSchoolsInfo(3L, "담당 지부", List.of(
                new ChapterWithSchoolsInfo.SchoolInfo(5L, "학교"))),
            new ChapterWithSchoolsInfo(4L, "다른 지부", List.of(
                new ChapterWithSchoolsInfo.SchoolInfo(6L, "다른 학교")))));
        // when
        NoticeViewerInfo result = sut.toMemberIdAndGisuId(MEMBER_ID, GISU_ID, null);
        // then
        assertThat(result.isChapterPresident()).isTrue();
        assertThat(result.chapterId()).isEqualTo(3L);
        assertThat(result.chapterSchoolIds()).containsExactly(5L);
        assertThat(result.viewerRole()).isEqualTo(NoticeTab.SCHOOL_CORE);
    }

    @Test
    void 선택한_교내_파트장의_학교와_담당_파트를_사용한다() {
        givenActiveRoles(
            ChallengerRoleInfo.builder().id(21L).roleType(ChallengerRoleType.SCHOOL_PART_LEADER)
                .organizationType(OrganizationType.SCHOOL).organizationId(5L)
                .responsiblePart(ChallengerPart.DESIGN).build(),
            ChallengerRoleInfo.builder().id(22L).roleType(ChallengerRoleType.CENTRAL_EDUCATION_TEAM_MEMBER)
                .organizationType(OrganizationType.CENTRAL).responsiblePart(ChallengerPart.PLAN).build());
        given(getChapterUseCase.byGisuAndSchool(GISU_ID, 5L)).willReturn(new ChapterInfo(3L, "지부"));

        NoticeViewerInfo result = sut.toMemberIdAndGisuId(MEMBER_ID, GISU_ID, 21L);

        assertThat(result.roleType()).isEqualTo(ChallengerRoleType.SCHOOL_PART_LEADER);
        assertThat(result.memberParts()).containsExactly(ChallengerPart.DESIGN);
        assertThat(result.schoolId()).isEqualTo(5L);
        assertThat(result.chapterId()).isEqualTo(3L);
    }

    @Test
    void 총괄_View로_과거_기수_조회_시에도_파트_필터에_쓰는_소속을_제공한다() {
        givenActiveRoles(ChallengerRoleInfo.builder().id(21L).roleType(ChallengerRoleType.CENTRAL_PRESIDENT)
            .organizationType(OrganizationType.CENTRAL).build());
        given(getMemberUseCase.findAllByIds(Set.of(MEMBER_ID)))
            .willReturn(Map.of(MEMBER_ID, MemberInfo.builder().id(MEMBER_ID).schoolId(5L).build()));
        given(getChapterUseCase.byGisuAndSchool(8L, 5L)).willReturn(new ChapterInfo(4L, "과거 지부"));

        NoticeViewerInfo result = sut.toMemberIdAndGisuId(MEMBER_ID, 8L, 21L);

        assertThat(result.viewerRole()).isEqualTo(NoticeTab.CENTRAL_MEMBER);
        assertThat(result.schoolId()).isEqualTo(5L);
        assertThat(result.chapterId()).isEqualTo(4L);
    }

    @Test
    void 교내_회장_View는_다른_기수의_공지_조회에_쓸_수_없다() {
        givenActiveRoles(ChallengerRoleInfo.builder().id(21L).roleType(ChallengerRoleType.SCHOOL_PRESIDENT)
            .organizationType(OrganizationType.SCHOOL).organizationId(5L).build());

        assertThatThrownBy(() -> sut.toMemberIdAndGisuId(MEMBER_ID, 8L, 21L))
            .isInstanceOfSatisfying(NoticeDomainException.class,
                exception -> assertThat(exception.getBaseCode()).isEqualTo(NoticeErrorCode.NO_READ_PERMISSION));
    }

    private void givenActiveRoles(ChallengerRoleInfo... roles) {
        given(getGisuUseCase.findActiveGisu()).willReturn(Optional.of(new GisuInfo(GISU_ID, 9L, null, null, true)));
        given(getChallengerRoleUseCase.listByMemberIdAndGisuId(MEMBER_ID, GISU_ID)).willReturn(List.of(roles));
    }
}
