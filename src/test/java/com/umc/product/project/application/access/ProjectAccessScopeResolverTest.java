package com.umc.product.project.application.access;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.umc.product.authorization.domain.RoleAttribute;
import com.umc.product.authorization.domain.SubjectAttributes;
import com.umc.product.authorization.domain.SubjectAttributes.GisuChallengerInfo;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.common.domain.enums.OrganizationType;
import com.umc.product.project.application.access.ProjectAccessScope.All;
import com.umc.product.project.application.access.ProjectAccessScope.ChapterScoped;
import com.umc.product.project.application.access.ProjectAccessScope.None;
import com.umc.product.project.application.access.ProjectAccessScope.OwnerOnly;
import com.umc.product.project.application.access.ProjectAccessScope.PublicOnly;
import com.umc.product.project.application.access.ProjectAccessScope.SchoolScoped;
import com.umc.product.project.application.port.out.LoadProjectPort;
import com.umc.product.project.domain.enums.ProjectStatus;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProjectAccessScopeResolverTest {

    @Mock
    LoadProjectPort loadProjectPort;

    @InjectMocks
    ProjectAccessScopeResolver sut;

    // --- resolveForPublicSearch ---

    @Test
    void publicSearch_은_총괄단이면_All() {
        SubjectAttributes subject = subjectWith(1L,
            List.of(role(ChallengerRoleType.CENTRAL_PRESIDENT, OrganizationType.CENTRAL, null, 1L)));
        Set<ProjectStatus> requested = Set.of(ProjectStatus.PENDING_REVIEW, ProjectStatus.IN_PROGRESS);

        ProjectAccessScope scope = sut.resolveForPublicSearch(subject, 1L, requested);

        assertThat(scope).isInstanceOf(All.class);
        assertThat(((All) scope).visibleStatuses()).containsExactlyInAnyOrderElementsOf(requested);
    }

    @Test
    void publicSearch_은_일반_챌린저면_PublicOnly() {
        SubjectAttributes subject = subjectWith(1L, List.of());
        Set<ProjectStatus> requested = Set.of(ProjectStatus.IN_PROGRESS);

        ProjectAccessScope scope = sut.resolveForPublicSearch(subject, 1L, requested);

        assertThat(scope).isInstanceOf(PublicOnly.class);
    }

    @Test
    void publicSearch_은_지부장이라도_PublicOnly() {
        SubjectAttributes subject = subjectWith(1L,
            List.of(role(ChallengerRoleType.CHAPTER_PRESIDENT, OrganizationType.CHAPTER, 5L, 1L)));
        Set<ProjectStatus> requested = Set.of(ProjectStatus.IN_PROGRESS);

        ProjectAccessScope scope = sut.resolveForPublicSearch(subject, 1L, requested);

        assertThat(scope).isInstanceOf(PublicOnly.class);
    }

    @Test
    void publicSearch_은_학교_회장이라도_PublicOnly() {
        SubjectAttributes subject = subjectWith(1L,
            List.of(role(ChallengerRoleType.SCHOOL_PRESIDENT, OrganizationType.SCHOOL, 7L, 1L)));
        Set<ProjectStatus> requested = Set.of(ProjectStatus.IN_PROGRESS);

        ProjectAccessScope scope = sut.resolveForPublicSearch(subject, 1L, requested);

        assertThat(scope).isInstanceOf(PublicOnly.class);
    }

    // --- resolveForManagement ---

    @Test
    void management_은_총괄단이면_All() {
        SubjectAttributes subject = subjectWith(1L,
            List.of(role(ChallengerRoleType.CENTRAL_PRESIDENT, OrganizationType.CENTRAL, null, 1L)));
        Set<ProjectStatus> requested = Set.of(ProjectStatus.PENDING_REVIEW, ProjectStatus.IN_PROGRESS);

        ProjectAccessScope scope = sut.resolveForManagement(subject, 1L, requested);

        assertThat(scope).isInstanceOf(All.class);
    }

    @Test
    void management_은_지부장이면_본인_지부_ChapterScoped() {
        SubjectAttributes subject = subjectWith(1L,
            List.of(role(ChallengerRoleType.CHAPTER_PRESIDENT, OrganizationType.CHAPTER, 5L, 1L)));
        Set<ProjectStatus> requested = Set.of(ProjectStatus.IN_PROGRESS);

        ProjectAccessScope scope = sut.resolveForManagement(subject, 1L, requested);

        assertThat(scope).isInstanceOf(ChapterScoped.class);
        assertThat(((ChapterScoped) scope).chapterId()).isEqualTo(5L);
    }

    @Test
    void management_은_학교_회장이면_본인_학교_SchoolScoped() {
        SubjectAttributes subject = subjectWith(1L,
            List.of(role(ChallengerRoleType.SCHOOL_PRESIDENT, OrganizationType.SCHOOL, 7L, 1L)));
        Set<ProjectStatus> requested = Set.of(ProjectStatus.IN_PROGRESS);

        ProjectAccessScope scope = sut.resolveForManagement(subject, 1L, requested);

        assertThat(scope).isInstanceOf(SchoolScoped.class);
        assertThat(((SchoolScoped) scope).schoolId()).isEqualTo(7L);
    }

    @Test
    void management_은_학교_부회장도_본인_학교_SchoolScoped() {
        SubjectAttributes subject = subjectWith(1L,
            List.of(role(ChallengerRoleType.SCHOOL_VICE_PRESIDENT, OrganizationType.SCHOOL, 7L, 1L)));
        Set<ProjectStatus> requested = Set.of(ProjectStatus.IN_PROGRESS);

        ProjectAccessScope scope = sut.resolveForManagement(subject, 1L, requested);

        assertThat(scope).isInstanceOf(SchoolScoped.class);
    }

    @Test
    void management_은_PM_챌린저면_OwnerOnly() {
        Long memberId = 10L;
        Long gisuId = 1L;
        given(loadProjectPort.existsByOwnerAndGisu(memberId, gisuId)).willReturn(true);

        SubjectAttributes subject = subjectWith(memberId, List.of());
        Set<ProjectStatus> requested = Set.of(ProjectStatus.DRAFT, ProjectStatus.IN_PROGRESS);

        ProjectAccessScope scope = sut.resolveForManagement(subject, gisuId, requested);

        assertThat(scope).isInstanceOf(OwnerOnly.class);
        assertThat(((OwnerOnly) scope).memberId()).isEqualTo(memberId);
    }

    @Test
    void management_은_관리_대상_없으면_None() {
        Long memberId = 10L;
        Long gisuId = 1L;
        given(loadProjectPort.existsByOwnerAndGisu(memberId, gisuId)).willReturn(false);

        SubjectAttributes subject = subjectWith(memberId, List.of());
        Set<ProjectStatus> requested = Set.of(ProjectStatus.IN_PROGRESS);

        ProjectAccessScope scope = sut.resolveForManagement(subject, gisuId, requested);

        assertThat(scope).isInstanceOf(None.class);
    }

    @Test
    void management_은_타_기수_지부장이면_PM_조회_경로로_빠진다() {
        Long memberId = 10L;
        Long currentGisuId = 2L;
        given(loadProjectPort.existsByOwnerAndGisu(memberId, currentGisuId)).willReturn(false);

        SubjectAttributes subject = subjectWith(memberId,
            List.of(role(ChallengerRoleType.CHAPTER_PRESIDENT, OrganizationType.CHAPTER, 5L, 1L)));
        Set<ProjectStatus> requested = Set.of(ProjectStatus.IN_PROGRESS);

        ProjectAccessScope scope = sut.resolveForManagement(subject, currentGisuId, requested);

        assertThat(scope).isInstanceOf(None.class);
    }

    @Test
    void management_은_총괄단_우선순위가_지부장보다_높다() {
        SubjectAttributes subject = subjectWith(1L, List.of(
            role(ChallengerRoleType.CENTRAL_PRESIDENT, OrganizationType.CENTRAL, null, 1L),
            role(ChallengerRoleType.CHAPTER_PRESIDENT, OrganizationType.CHAPTER, 5L, 1L)
        ));
        Set<ProjectStatus> requested = Set.of(ProjectStatus.IN_PROGRESS);

        ProjectAccessScope scope = sut.resolveForManagement(subject, 1L, requested);

        assertThat(scope).isInstanceOf(All.class);
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
}
