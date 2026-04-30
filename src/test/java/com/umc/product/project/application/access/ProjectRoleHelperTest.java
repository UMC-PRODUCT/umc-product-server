package com.umc.product.project.application.access;

import static org.assertj.core.api.Assertions.assertThat;

import com.umc.product.authorization.domain.RoleAttribute;
import com.umc.product.authorization.domain.SubjectAttributes;
import com.umc.product.authorization.domain.SubjectAttributes.GisuChallengerInfo;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.common.domain.enums.OrganizationType;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.enums.ProjectStatus;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class ProjectRoleHelperTest {

    // --- isCentralCore ---

    @Test
    void isCentralCore_은_총괄_역할이_있으면_true() {
        SubjectAttributes subject = subjectWith(1L,
            List.of(role(ChallengerRoleType.CENTRAL_PRESIDENT, OrganizationType.CENTRAL, null, 1L)));

        assertThat(ProjectRoleHelper.isCentralCore(subject)).isTrue();
    }

    @Test
    void isCentralCore_은_부총괄_역할이_있으면_true() {
        SubjectAttributes subject = subjectWith(1L,
            List.of(role(ChallengerRoleType.CENTRAL_VICE_PRESIDENT, OrganizationType.CENTRAL, null, 1L)));

        assertThat(ProjectRoleHelper.isCentralCore(subject)).isTrue();
    }

    @Test
    void isCentralCore_은_SUPER_ADMIN이면_true() {
        SubjectAttributes subject = subjectWith(1L,
            List.of(role(ChallengerRoleType.SUPER_ADMIN, OrganizationType.CENTRAL, null, 1L)));

        assertThat(ProjectRoleHelper.isCentralCore(subject)).isTrue();
    }

    @Test
    void isCentralCore_은_운영국원이면_false() {
        SubjectAttributes subject = subjectWith(1L,
            List.of(role(ChallengerRoleType.CENTRAL_OPERATING_TEAM_MEMBER, OrganizationType.CENTRAL, null, 1L)));

        assertThat(ProjectRoleHelper.isCentralCore(subject)).isFalse();
    }

    @Test
    void isCentralCore_은_역할이_없으면_false() {
        SubjectAttributes subject = subjectWith(1L, List.of());

        assertThat(ProjectRoleHelper.isCentralCore(subject)).isFalse();
    }

    // --- isOwner ---

    @Test
    void isOwner_는_memberId가_일치하면_true() {
        Long memberId = 10L;
        Project project = project(100L, memberId, ProjectStatus.IN_PROGRESS);
        SubjectAttributes subject = subjectWith(memberId, List.of());

        assertThat(ProjectRoleHelper.isOwner(subject, project)).isTrue();
    }

    @Test
    void isOwner_는_memberId가_다르면_false() {
        Project project = project(100L, 10L, ProjectStatus.IN_PROGRESS);
        SubjectAttributes subject = subjectWith(20L, List.of());

        assertThat(ProjectRoleHelper.isOwner(subject, project)).isFalse();
    }

    // --- chapterPresidentOrgId ---

    @Test
    void chapterPresidentOrgId_는_지부장_역할이면_관할_지부ID_반환() {
        SubjectAttributes subject = subjectWith(1L,
            List.of(role(ChallengerRoleType.CHAPTER_PRESIDENT, OrganizationType.CHAPTER, 5L, 1L)));

        assertThat(ProjectRoleHelper.chapterPresidentOrgId(subject, 1L))
            .contains(5L);
    }

    @Test
    void chapterPresidentOrgId_는_기수가_다르면_empty() {
        SubjectAttributes subject = subjectWith(1L,
            List.of(role(ChallengerRoleType.CHAPTER_PRESIDENT, OrganizationType.CHAPTER, 5L, 1L)));

        assertThat(ProjectRoleHelper.chapterPresidentOrgId(subject, 2L))
            .isEmpty();
    }

    @Test
    void chapterPresidentOrgId_는_지부장_아니면_empty() {
        SubjectAttributes subject = subjectWith(1L,
            List.of(role(ChallengerRoleType.SCHOOL_PRESIDENT, OrganizationType.SCHOOL, 5L, 1L)));

        assertThat(ProjectRoleHelper.chapterPresidentOrgId(subject, 1L))
            .isEmpty();
    }

    // --- schoolCoreOrgId ---

    @Test
    void schoolCoreOrgId_는_회장_역할이면_학교ID_반환() {
        SubjectAttributes subject = subjectWith(1L,
            List.of(role(ChallengerRoleType.SCHOOL_PRESIDENT, OrganizationType.SCHOOL, 7L, 1L)));

        assertThat(ProjectRoleHelper.schoolCoreOrgId(subject, 1L))
            .contains(7L);
    }

    @Test
    void schoolCoreOrgId_는_부회장_역할이면_학교ID_반환() {
        SubjectAttributes subject = subjectWith(1L,
            List.of(role(ChallengerRoleType.SCHOOL_VICE_PRESIDENT, OrganizationType.SCHOOL, 7L, 1L)));

        assertThat(ProjectRoleHelper.schoolCoreOrgId(subject, 1L))
            .contains(7L);
    }

    @Test
    void schoolCoreOrgId_는_파트장이면_empty() {
        SubjectAttributes subject = subjectWith(1L,
            List.of(role(ChallengerRoleType.SCHOOL_PART_LEADER, OrganizationType.SCHOOL, 7L, 1L)));

        assertThat(ProjectRoleHelper.schoolCoreOrgId(subject, 1L))
            .isEmpty();
    }

    @Test
    void schoolCoreOrgId_는_기수가_다르면_empty() {
        SubjectAttributes subject = subjectWith(1L,
            List.of(role(ChallengerRoleType.SCHOOL_PRESIDENT, OrganizationType.SCHOOL, 7L, 1L)));

        assertThat(ProjectRoleHelper.schoolCoreOrgId(subject, 2L))
            .isEmpty();
    }

    @Test
    void schoolCoreOrgId_는_SUPER_ADMIN만_있으면_empty() {
        SubjectAttributes subject = subjectWith(1L,
            List.of(role(ChallengerRoleType.SUPER_ADMIN, OrganizationType.CENTRAL, null, 1L)));

        assertThat(ProjectRoleHelper.schoolCoreOrgId(subject, 1L))
            .isEmpty();
    }

    // --- canSeeFullInfo ---

    @Test
    void canSeeFullInfo_는_총괄단이면_true() {
        Project project = project(100L, 10L, ProjectStatus.IN_PROGRESS);
        SubjectAttributes subject = subjectWith(20L,
            List.of(role(ChallengerRoleType.CENTRAL_PRESIDENT, OrganizationType.CENTRAL, null, 1L)));

        assertThat(ProjectRoleHelper.canSeeFullInfo(subject, project)).isTrue();
    }

    @Test
    void canSeeFullInfo_는_PM_본인이면_true() {
        Long memberId = 10L;
        Project project = project(100L, memberId, ProjectStatus.IN_PROGRESS);
        SubjectAttributes subject = subjectWith(memberId, List.of());

        assertThat(ProjectRoleHelper.canSeeFullInfo(subject, project)).isTrue();
    }

    @Test
    void canSeeFullInfo_는_외부인이면_false() {
        Project project = project(100L, 10L, ProjectStatus.IN_PROGRESS);
        SubjectAttributes subject = subjectWith(20L, List.of());

        assertThat(ProjectRoleHelper.canSeeFullInfo(subject, project)).isFalse();
    }

    @Test
    void canSeeFullInfo_는_운영국원이면_false() {
        Project project = project(100L, 10L, ProjectStatus.IN_PROGRESS);
        SubjectAttributes subject = subjectWith(20L,
            List.of(role(ChallengerRoleType.CENTRAL_OPERATING_TEAM_MEMBER, OrganizationType.CENTRAL, null, 1L)));

        assertThat(ProjectRoleHelper.canSeeFullInfo(subject, project)).isFalse();
    }

    // --- helpers ---

    private SubjectAttributes subjectWith(Long memberId, List<RoleAttribute> roles) {
        return SubjectAttributes.builder()
            .memberId(memberId)
            .schoolId(1L)
            .gisuChallengerInfos(List.<GisuChallengerInfo>of())
            .roleAttributes(roles)
            .build();
    }

    private RoleAttribute role(ChallengerRoleType type, OrganizationType orgType, Long orgId, Long gisuId) {
        return new RoleAttribute(type, orgType, orgId, null, gisuId);
    }

    private Project project(Long id, Long ownerMemberId, ProjectStatus status) {
        try {
            var constructor = Project.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            Project project = constructor.newInstance();
            ReflectionTestUtils.setField(project, "id", id);
            ReflectionTestUtils.setField(project, "productOwnerMemberId", ownerMemberId);
            ReflectionTestUtils.setField(project, "status", status);
            ReflectionTestUtils.setField(project, "gisuId", 1L);
            ReflectionTestUtils.setField(project, "chapterId", 1L);
            return project;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
