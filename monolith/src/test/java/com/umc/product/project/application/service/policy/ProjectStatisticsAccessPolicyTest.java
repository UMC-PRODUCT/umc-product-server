package com.umc.product.project.application.service.policy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.umc.product.authorization.application.port.in.CheckPermissionUseCase;
import com.umc.product.authorization.domain.RoleAttribute;
import com.umc.product.authorization.domain.SubjectAttributes;
import com.umc.product.authorization.domain.SystemRoleType;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.common.domain.enums.OrganizationType;
import com.umc.product.member.application.port.in.query.CheckMemberExistenceUseCase;
import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import com.umc.product.organization.application.port.in.query.dto.chapter.ChapterWithSchoolsInfo;
import com.umc.product.organization.application.port.in.query.dto.chapter.ChapterWithSchoolsInfo.SchoolInfo;
import com.umc.product.organization.exception.OrganizationDomainException;
import com.umc.product.organization.exception.OrganizationErrorCode;
import com.umc.product.project.application.port.out.LoadProjectMemberPort;
import com.umc.product.project.domain.Project;

@ExtendWith(MockitoExtension.class)
class ProjectStatisticsAccessPolicyTest {

    private static final Long MEMBER_ID = 10L;
    private static final Long PROJECT_ID = 100L;
    private static final Long CHAPTER_ID = 7L;
    private static final Long SCHOOL_ID = 3L;

    @Mock
    LoadProjectMemberPort loadProjectMemberPort;
    @Mock
    CheckPermissionUseCase checkPermissionUseCase;
    @Mock
    CheckMemberExistenceUseCase checkMemberExistenceUseCase;
    @Mock
    GetChapterUseCase getChapterUseCase;

    @InjectMocks
    ProjectStatisticsAccessPolicy sut;

    @Test
    @DisplayName("프로젝트 PO는 통계를 조회할 수 있다")
    void 프로젝트_PO는_통계를_조회할_수_있다() {
        Project project = project(MEMBER_ID);

        boolean result = sut.canReadProjectStatistics(MEMBER_ID, project);

        assertThat(result).isTrue();
        verifyNoInteractions(loadProjectMemberPort, checkPermissionUseCase, checkMemberExistenceUseCase, getChapterUseCase);
    }

    @Test
    @DisplayName("보조 PM은 프로젝트 통계를 조회할 수 있다")
    void 보조_PM은_프로젝트_통계를_조회할_수_있다() {
        Project project = project(999L);
        given(loadProjectMemberPort.isActivePlanMember(PROJECT_ID, MEMBER_ID)).willReturn(true);

        boolean result = sut.canReadProjectStatistics(MEMBER_ID, project);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("중앙 운영진은 지부 통계를 조회할 수 있다")
    void 중앙_운영진은_지부_통계를_조회할_수_있다() {
        given(getChapterUseCase.getGisuIdByChapterId(CHAPTER_ID)).willReturn(1L);
        givenSubject(role(ChallengerRoleType.CENTRAL_PRESIDENT, OrganizationType.CENTRAL, null));

        boolean result = sut.canReadChapterStatistics(MEMBER_ID, CHAPTER_ID);

        assertThat(result).isTrue();
        verify(checkPermissionUseCase).loadSubject(MEMBER_ID);
    }

    @Test
    @DisplayName("해당 지부장은 지부 통계를 조회할 수 있다")
    void 해당_지부장은_지부_통계를_조회할_수_있다() {
        given(getChapterUseCase.getGisuIdByChapterId(CHAPTER_ID)).willReturn(1L);
        givenSubject(role(ChallengerRoleType.CHAPTER_PRESIDENT, OrganizationType.CHAPTER, CHAPTER_ID));

        boolean result = sut.canReadChapterStatistics(MEMBER_ID, CHAPTER_ID);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("해당 지부 소속 학교 회장단은 지부 통계를 조회할 수 있다")
    void 해당_지부_소속_학교_회장단은_지부_통계를_조회할_수_있다() {
        given(getChapterUseCase.getGisuIdByChapterId(CHAPTER_ID)).willReturn(1L);
        givenSubject(role(ChallengerRoleType.SCHOOL_PRESIDENT, OrganizationType.SCHOOL, SCHOOL_ID));
        givenChapterSchools();

        boolean result = sut.canReadChapterStatistics(MEMBER_ID, CHAPTER_ID);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("역할이 없으면 지부 통계를 조회할 수 없다")
    void 역할이_없으면_지부_통계를_조회할_수_없다() {
        givenSubject();

        boolean result = sut.canReadChapterStatistics(MEMBER_ID, CHAPTER_ID);

        assertThat(result).isFalse();
        verifyNoInteractions(getChapterUseCase);
        verify(checkPermissionUseCase).loadSubject(MEMBER_ID);
    }

    @Test
    @DisplayName("이전 기수 총괄단 역할은 프로젝트와 지부 통계 권한으로 인정하지 않는다")
    void 이전_기수_총괄단_거부() {
        // given
        given(getChapterUseCase.getGisuIdByChapterId(CHAPTER_ID)).willReturn(1L);
        givenSubject(
            new RoleAttribute(ChallengerRoleType.CENTRAL_PRESIDENT, OrganizationType.CENTRAL, null, null, 2L));
        // when & then
        assertThat(sut.canReadProjectStatistics(MEMBER_ID, project(999L))).isFalse();
        assertThat(sut.canReadChapterStatistics(MEMBER_ID, CHAPTER_ID)).isFalse();
    }

    @Test
    @DisplayName("이전 기수 학교 회장 역할은 같은 학교여도 통계를 조회할 수 없다")
    void 이전_기수_학교회장_거부() {
        // given
        givenSubject(
            new RoleAttribute(ChallengerRoleType.SCHOOL_PRESIDENT, OrganizationType.SCHOOL, SCHOOL_ID, null, 2L));
        givenChapterSchools();

        // when & then
        assertThat(sut.canReadProjectStatistics(MEMBER_ID, project(999L))).isFalse();
    }

    @Test
    @DisplayName("최고관리자는 기수와 무관하게 지부 통계를 조회할 수 있다")
    void 최고관리자_전역_권한_유지() {
        given(checkMemberExistenceUseCase.existsById(MEMBER_ID)).willReturn(true);
        given(checkPermissionUseCase.loadSubject(MEMBER_ID)).willReturn(SubjectAttributes.builder()
            .memberId(MEMBER_ID).systemRoles(Set.of(SystemRoleType.SUPER_ADMIN)).build());
        assertThat(sut.canReadChapterStatistics(MEMBER_ID, CHAPTER_ID)).isTrue();
        assertThat(sut.canReadProjectStatistics(MEMBER_ID, project(999L))).isTrue();
        verifyNoInteractions(getChapterUseCase);
        verify(checkPermissionUseCase, times(2)).loadSubject(MEMBER_ID);
    }

    @Test
    @DisplayName("통계 권한에 해당하지 않는 역할은 지부 존재 여부를 조회하지 않고 거절한다")
    void 권한_없는_역할은_지부_조회_전에_거절한다() {
        // given
        givenSubject(
            role(ChallengerRoleType.CENTRAL_EDUCATION_TEAM_MEMBER, OrganizationType.CENTRAL, null));

        // when & then
        assertThat(sut.canReadChapterStatistics(MEMBER_ID, 999L)).isFalse();
        verifyNoInteractions(getChapterUseCase);
        verify(checkPermissionUseCase).loadSubject(MEMBER_ID);
    }

    @Test
    @DisplayName("운영진이 존재하지 않는 지부를 조회하면 지부 없음 오류를 유지한다")
    void 운영진의_없는_지부_조회는_대상_없음_오류를_유지한다() {
        // given
        givenSubject(
            role(ChallengerRoleType.CENTRAL_PRESIDENT, OrganizationType.CENTRAL, null));
        given(getChapterUseCase.getGisuIdByChapterId(999L))
            .willThrow(new OrganizationDomainException(OrganizationErrorCode.CHAPTER_NOT_FOUND));

        // when & then
        assertThatThrownBy(() -> sut.canReadChapterStatistics(MEMBER_ID, 999L))
            .isInstanceOfSatisfying(OrganizationDomainException.class,
                exception -> assertThat(exception.getBaseCode()).isEqualTo(OrganizationErrorCode.CHAPTER_NOT_FOUND));
        verify(checkPermissionUseCase).loadSubject(MEMBER_ID);
    }

    @Test
    @DisplayName("존재하지 않는 회원은 권한 스냅샷을 조회하지 않고 거절한다")
    void 존재하지_않는_회원은_거절한다() {
        // given
        given(checkMemberExistenceUseCase.existsById(MEMBER_ID)).willReturn(false);

        // when
        boolean result = sut.canReadChapterStatistics(MEMBER_ID, CHAPTER_ID);

        // then
        assertThat(result).isFalse();
        verifyNoInteractions(checkPermissionUseCase, getChapterUseCase);
    }

    @Test
    @DisplayName("다른 지부의 지부장은 대상 지부 통계를 조회할 수 없다")
    void 다른_지부의_지부장은_거절한다() {
        // given
        givenSubject(role(ChallengerRoleType.CHAPTER_PRESIDENT, OrganizationType.CHAPTER, 999L));
        given(getChapterUseCase.getGisuIdByChapterId(CHAPTER_ID)).willReturn(1L);
        givenChapterSchools();

        // when
        boolean result = sut.canReadChapterStatistics(MEMBER_ID, CHAPTER_ID);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("다른 지부 소속 학교 회장은 대상 지부 통계를 조회할 수 없다")
    void 다른_지부_소속_학교회장은_거절한다() {
        // given
        givenSubject(role(ChallengerRoleType.SCHOOL_PRESIDENT, OrganizationType.SCHOOL, SCHOOL_ID));
        given(getChapterUseCase.getGisuIdByChapterId(CHAPTER_ID)).willReturn(1L);
        given(getChapterUseCase.getChaptersWithSchoolsByGisuId(1L)).willReturn(List.of(
            new ChapterWithSchoolsInfo(CHAPTER_ID, "대상 지부", List.of(new SchoolInfo(99L, "다른 학교"))),
            new ChapterWithSchoolsInfo(999L, "다른 지부", List.of(new SchoolInfo(SCHOOL_ID, "소속 학교")))));

        // when
        boolean result = sut.canReadChapterStatistics(MEMBER_ID, CHAPTER_ID);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("지부장 역할의 조직 유형이 지부가 아니면 거절한다")
    void 지부장_조직_유형이_다르면_거절한다() {
        // given
        givenSubject(role(ChallengerRoleType.CHAPTER_PRESIDENT, OrganizationType.SCHOOL, CHAPTER_ID));

        // when
        boolean result = sut.canReadChapterStatistics(MEMBER_ID, CHAPTER_ID);

        // then
        assertThat(result).isFalse();
        verifyNoInteractions(getChapterUseCase);
    }

    @Test
    @DisplayName("학교 회장 역할의 조직 유형이 학교가 아니면 거절한다")
    void 학교회장_조직_유형이_다르면_거절한다() {
        // given
        givenSubject(role(ChallengerRoleType.SCHOOL_PRESIDENT, OrganizationType.CHAPTER, SCHOOL_ID));

        // when
        boolean result = sut.canReadChapterStatistics(MEMBER_ID, CHAPTER_ID);

        // then
        assertThat(result).isFalse();
        verifyNoInteractions(getChapterUseCase);
    }

    private static Project project(Long ownerMemberId) {
        Project project = Project.createDraft(1L, CHAPTER_ID, ownerMemberId, 1L, ownerMemberId);
        ReflectionTestUtils.setField(project, "id", PROJECT_ID);
        return project;
    }

    private void givenSubject(RoleAttribute... roles) {
        given(checkMemberExistenceUseCase.existsById(MEMBER_ID)).willReturn(true);
        given(checkPermissionUseCase.loadSubject(MEMBER_ID)).willReturn(SubjectAttributes.builder()
            .memberId(MEMBER_ID).roleAttributes(List.of(roles)).build());
    }

    private void givenChapterSchools() {
        given(getChapterUseCase.getChaptersWithSchoolsByGisuId(1L)).willReturn(List.of(
            new ChapterWithSchoolsInfo(CHAPTER_ID, "테스트 지부", List.of(new SchoolInfo(SCHOOL_ID, "테스트 학교")))));
    }

    private static RoleAttribute role(
        ChallengerRoleType roleType,
        OrganizationType organizationType,
        Long organizationId
    ) {
        return new RoleAttribute(roleType, organizationType, organizationId, null, 1L);
    }
}
