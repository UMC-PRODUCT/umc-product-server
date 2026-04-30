package com.umc.product.project.application.access;

import com.umc.product.authorization.domain.RoleAttribute;
import com.umc.product.authorization.domain.SubjectAttributes;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.project.domain.Project;
import java.util.Objects;
import java.util.Optional;

/**
 * Project 도메인의 역할 판정 헬퍼.
 * <p>
 * L2 ({@code ProjectPermissionEvaluator}) 와 L3-A ({@code ProjectAccessScopeResolver}) 양쪽에서
 * 동일한 역할 판정이 필요하므로 한 곳에 모아 재사용한다.
 */
public final class ProjectRoleHelper {

    private ProjectRoleHelper() {
    }

    /** 중앙운영사무국 총괄단(총괄/부총괄/SUPER_ADMIN) 여부. */
    public static boolean isCentralCore(SubjectAttributes subject) {
        return subject.roleAttributes().stream()
            .anyMatch(role -> role.roleType().isAtLeastCentralCore());
    }

    /** 본 프로젝트 PM 여부. */
    public static boolean isOwner(SubjectAttributes subject, Project project) {
        return Objects.equals(subject.memberId(), project.getProductOwnerMemberId());
    }

    /** 지부장으로 관할하는 지부 ID (특정 기수 한정). 역할은 기수별로 다르므로 gisuId 필수. */
    public static Optional<Long> chapterPresidentOrgId(SubjectAttributes subject, Long gisuId) {
        return subject.roleAttributes().stream()
            .filter(role -> role.roleType() == ChallengerRoleType.CHAPTER_PRESIDENT)
            .filter(role -> Objects.equals(role.gisuId(), gisuId))
            .map(RoleAttribute::organizationId)
            .findFirst();
    }

    /**
     * 학교 회장단(회장/부회장)으로 관할하는 학교 ID (특정 기수 한정).
     * <p>
     * SUPER_ADMIN 은 의도적으로 제외한다. SUPER_ADMIN 의 organizationId 는 null 인 경우가 많고
     * Central Core 분기에서 먼저 처리되므로, 이 헬퍼는 학교 단위 회장/부회장만 매칭한다.
     */
    public static Optional<Long> schoolCoreOrgId(SubjectAttributes subject, Long gisuId) {
        return subject.roleAttributes().stream()
            .filter(role -> role.roleType() == ChallengerRoleType.SCHOOL_PRESIDENT
                || role.roleType() == ChallengerRoleType.SCHOOL_VICE_PRESIDENT)
            .filter(role -> Objects.equals(role.gisuId(), gisuId))
            .map(RoleAttribute::organizationId)
            .findFirst();
    }

    /**
     * 마스킹 결정 — 본 프로젝트의 실명/연락처 등 민감 정보를 볼 수 있는지.
     * <p>
     * 현 시점에는 PM 본인 + Central Core 만 권한자로 본다.
     * 보조 PM / 다른 파트 팀원도 권한자에 포함하려면 별도 멤버 조회가 필요하므로 후속 PR 에서 확장.
     */
    public static boolean canSeeFullInfo(SubjectAttributes subject, Project project) {
        if (isCentralCore(subject)) {
            return true;
        }
        return isOwner(subject, project);
    }
}
