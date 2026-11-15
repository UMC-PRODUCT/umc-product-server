package com.umc.product.challenger.application.service.evaluator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourcePermission;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.authorization.domain.RoleAttribute;
import com.umc.product.authorization.domain.SubjectAttributes;
import com.umc.product.authorization.domain.SystemRoleType;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.common.domain.enums.OrganizationType;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.dto.MemberInfo;
import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import com.umc.product.organization.application.port.in.query.dto.chapter.ChapterInfo;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChallengerPermissionEvaluator")
class ChallengerPermissionEvaluatorTest {

    private static final Long CHALLENGER_ID = 10L;
    private static final Long TARGET_MEMBER_ID = 20L;
    private static final Long TARGET_GISU_ID = 30L;
    private static final Long TARGET_SCHOOL_ID = 40L;
    private static final Long TARGET_CHAPTER_ID = 50L;

    @Mock
    GetChallengerUseCase getChallengerUseCase;

    @Mock
    GetMemberUseCase getMemberUseCase;

    @Mock
    GetChapterUseCase getChapterUseCase;

    ChallengerPermissionEvaluator sut;

    @BeforeEach
    void setUp() {
        sut = new ChallengerPermissionEvaluator(getChallengerUseCase, getMemberUseCase, getChapterUseCase);
        lenient().when(getChallengerUseCase.getById(CHALLENGER_ID)).thenReturn(ChallengerInfo.builder()
            .challengerId(CHALLENGER_ID)
            .memberId(TARGET_MEMBER_ID)
            .gisuId(TARGET_GISU_ID)
            .build());
        lenient().when(getMemberUseCase.getById(TARGET_MEMBER_ID)).thenReturn(MemberInfo.builder()
            .id(TARGET_MEMBER_ID)
            .schoolId(TARGET_SCHOOL_ID)
            .build());
    }

    @Test
    @DisplayName("SUPER_ADMIN은 챌린저 상세를 조회할 수 있다")
    void super_admin_can_read_challenger() {
        SubjectAttributes subject = subject(List.of(), Set.of(SystemRoleType.SUPER_ADMIN));

        assertThat(evaluateRead(subject)).isTrue();
    }

    @Test
    @DisplayName("대상 기수 중앙운영사무국은 챌린저 상세를 조회할 수 있다")
    void central_member_in_target_gisu_can_read_challenger() {
        SubjectAttributes subject = subject(List.of(role(
            ChallengerRoleType.CENTRAL_OPERATING_TEAM_MEMBER,
            OrganizationType.CENTRAL,
            null,
            TARGET_GISU_ID
        )), Set.of());

        assertThat(evaluateRead(subject)).isTrue();
    }

    @Test
    @DisplayName("대상 기수의 동일 학교 운영진은 챌린저 상세를 조회할 수 있다")
    void school_admin_in_target_school_and_gisu_can_read_challenger() {
        SubjectAttributes subject = subject(List.of(role(
            ChallengerRoleType.SCHOOL_ETC_ADMIN,
            OrganizationType.SCHOOL,
            TARGET_SCHOOL_ID,
            TARGET_GISU_ID
        )), Set.of());

        assertThat(evaluateRead(subject)).isTrue();
    }

    @Test
    @DisplayName("대상 기수의 동일 지부 지부장은 챌린저 상세를 조회할 수 있다")
    void chapter_president_in_target_chapter_and_gisu_can_read_challenger() {
        given(getChapterUseCase.findByGisuAndSchool(TARGET_GISU_ID, TARGET_SCHOOL_ID))
            .willReturn(java.util.Optional.of(new ChapterInfo(TARGET_CHAPTER_ID, "테스트 지부")));
        SubjectAttributes subject = subject(List.of(role(
            ChallengerRoleType.CHAPTER_PRESIDENT,
            OrganizationType.CHAPTER,
            TARGET_CHAPTER_ID,
            TARGET_GISU_ID
        )), Set.of());

        assertThat(evaluateRead(subject)).isTrue();
    }

    @Test
    @DisplayName("다른 학교 회장단은 챌린저 상세를 조회할 수 없다")
    void school_core_in_other_school_cannot_read_challenger() {
        SubjectAttributes subject = subject(List.of(role(
            ChallengerRoleType.SCHOOL_VICE_PRESIDENT,
            OrganizationType.SCHOOL,
            999L,
            TARGET_GISU_ID
        )), Set.of());

        assertThat(evaluateRead(subject)).isFalse();
    }

    @Test
    @DisplayName("다른 기수 운영진과 다른 지부 지부장은 챌린저 상세를 조회할 수 없다")
    void unauthorized_roles_cannot_read_challenger() {
        SubjectAttributes subject = subject(List.of(
            role(ChallengerRoleType.CENTRAL_EDUCATION_TEAM_MEMBER, OrganizationType.CENTRAL, null, 999L),
            role(ChallengerRoleType.CHAPTER_PRESIDENT, OrganizationType.CHAPTER, 999L, TARGET_GISU_ID)
        ), Set.of());

        assertThat(evaluateRead(subject)).isFalse();
    }

    @Test
    @DisplayName("다른 지부 지부장은 챌린저 상세를 조회할 수 없다")
    void chapterPresidentInOtherChapterCannotReadChallenger() {
        given(getChapterUseCase.findByGisuAndSchool(TARGET_GISU_ID, TARGET_SCHOOL_ID))
            .willReturn(java.util.Optional.of(new ChapterInfo(TARGET_CHAPTER_ID, "테스트 지부")));
        SubjectAttributes subject = subject(List.of(role(
            ChallengerRoleType.CHAPTER_PRESIDENT,
            OrganizationType.CHAPTER,
            999L,
            TARGET_GISU_ID
        )), Set.of());

        assertThat(evaluateRead(subject)).isFalse();
    }

    @Test
    @DisplayName("다른 학교 기타 운영진은 챌린저 상세를 조회할 수 없다")
    void schoolStaffInOtherSchoolCannotReadChallenger() {
        SubjectAttributes subject = subject(List.of(role(
            ChallengerRoleType.SCHOOL_ETC_ADMIN,
            OrganizationType.SCHOOL,
            999L,
            TARGET_GISU_ID
        )), Set.of());

        assertThat(evaluateRead(subject)).isFalse();
    }

    @Test
    @DisplayName("동일 학교 기타 운영진은 챌린저를 조회할 수 있지만 관리할 수 없다")
    void schoolEtcAdminCanReadButCannotManageChallenger() {
        SubjectAttributes subject = subject(List.of(role(
            ChallengerRoleType.SCHOOL_ETC_ADMIN,
            OrganizationType.SCHOOL,
            TARGET_SCHOOL_ID,
            TARGET_GISU_ID
        )), Set.of());

        assertThat(evaluateRead(subject)).isTrue();
        assertThat(evaluateManage(subject)).isFalse();
    }

    @Test
    @DisplayName("동일 지부 지부장은 챌린저를 조회할 수 있지만 관리할 수 없다")
    void chapterPresidentCanReadButCannotManageChallenger() {
        given(getChapterUseCase.findByGisuAndSchool(TARGET_GISU_ID, TARGET_SCHOOL_ID))
            .willReturn(java.util.Optional.of(new ChapterInfo(TARGET_CHAPTER_ID, "테스트 지부")));
        SubjectAttributes subject = subject(List.of(role(
            ChallengerRoleType.CHAPTER_PRESIDENT,
            OrganizationType.CHAPTER,
            TARGET_CHAPTER_ID,
            TARGET_GISU_ID
        )), Set.of());

        assertThat(evaluateRead(subject)).isTrue();
        assertThat(evaluateManage(subject)).isFalse();
    }

    @Test
    @DisplayName("동일 학교 회장단은 기존과 동일하게 챌린저를 관리할 수 있다")
    void schoolCoreCanManageChallenger() {
        SubjectAttributes subject = subject(List.of(role(
            ChallengerRoleType.SCHOOL_PRESIDENT,
            OrganizationType.SCHOOL,
            TARGET_SCHOOL_ID,
            TARGET_GISU_ID
        )), Set.of());

        assertThat(evaluateManage(subject)).isTrue();
    }

    @Test
    @DisplayName("대상 기수 중앙 운영진은 챌린저를 관리할 수 있다")
    void centralMemberInTargetGisuCanManageChallenger() {
        SubjectAttributes subject = subject(List.of(role(
            ChallengerRoleType.CENTRAL_OPERATING_TEAM_MEMBER,
            OrganizationType.CENTRAL,
            null,
            TARGET_GISU_ID
        )), Set.of());

        assertThat(evaluateManage(subject)).isTrue();
    }

    @Test
    @DisplayName("다른 기수 중앙 운영진은 챌린저를 관리할 수 없다")
    void centralMemberInOtherGisuCannotManageChallenger() {
        SubjectAttributes subject = subject(List.of(role(
            ChallengerRoleType.CENTRAL_EDUCATION_TEAM_MEMBER,
            OrganizationType.CENTRAL,
            null,
            999L
        )), Set.of());

        assertThat(evaluateManage(subject)).isFalse();
    }

    @Test
    @DisplayName("중앙 총괄은 다른 기수의 챌린저도 관리할 수 있다")
    void centralCoreCanManageChallengerInOtherGisu() {
        SubjectAttributes subject = subject(List.of(role(
            ChallengerRoleType.CENTRAL_PRESIDENT,
            OrganizationType.CENTRAL,
            null,
            999L
        )), Set.of());

        assertThat(evaluateManage(subject)).isTrue();
    }

    private boolean evaluateRead(SubjectAttributes subject) {
        return sut.evaluate(
            subject,
            ResourcePermission.of(ResourceType.CHALLENGER, CHALLENGER_ID, PermissionType.READ)
        );
    }

    private boolean evaluateManage(SubjectAttributes subject) {
        return sut.evaluate(
            subject,
            ResourcePermission.of(ResourceType.CHALLENGER, CHALLENGER_ID, PermissionType.MANAGE)
        );
    }

    private SubjectAttributes subject(List<RoleAttribute> roles, Set<SystemRoleType> systemRoles) {
        return SubjectAttributes.builder()
            .memberId(1L)
            .schoolId(TARGET_SCHOOL_ID)
            .gisuChallengerInfos(List.of())
            .roleAttributes(roles)
            .systemRoles(systemRoles)
            .build();
    }

    private RoleAttribute role(
        ChallengerRoleType roleType,
        OrganizationType organizationType,
        Long organizationId,
        Long gisuId
    ) {
        return new RoleAttribute(roleType, organizationType, organizationId, null, gisuId);
    }
}
