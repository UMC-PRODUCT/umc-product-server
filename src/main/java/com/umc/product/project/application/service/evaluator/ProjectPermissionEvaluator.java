package com.umc.product.project.application.service.evaluator;

import com.umc.product.authorization.application.port.out.ResourcePermissionEvaluator;
import com.umc.product.authorization.domain.ResourcePermission;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.authorization.domain.SubjectAttributes;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.application.access.ProjectRoleHelper;
import com.umc.product.project.application.port.out.LoadProjectPort;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Project 도메인 단건 액션의 권한 판정 (L2). status × 역할 binary 매트릭스만 다룬다.
 * <p>
 * 가시 범위(scoping)는 {@code ProjectAccessScopeResolver}, 응답 마스킹은
 * {@code ProjectRoleHelper#canSeeFullInfo} + Service 분기에서 처리.
 */
@Component
@RequiredArgsConstructor
public class ProjectPermissionEvaluator implements ResourcePermissionEvaluator {

    private final LoadProjectPort loadProjectPort;

    @Override
    public ResourceType supportedResourceType() {
        return ResourceType.PROJECT;
    }

    @Override
    public boolean evaluate(SubjectAttributes subject, ResourcePermission permission) {
        return switch (permission.permission()) {
            case READ -> canRead(subject, permission);
            case WRITE -> canWrite(subject);
            case EDIT -> canEdit(subject, permission);
            case DELETE -> ProjectRoleHelper.isCentralCore(subject);
            default -> false;
        };
    }

    /**
     * 단건 READ 분기. 비공개 상태(DRAFT/PENDING_REVIEW/ABORTED)는 권한자만 노출.
     * <p>
     * 목록 조회는 {@code resourceId} 가 없어 이 분기를 타지 않고 단순 통과 후 L3-A scope 에서 거른다.
     */
    private boolean canRead(SubjectAttributes subject, ResourcePermission permission) {
        if (permission.resourceId() == null) {
            return true;
        }
        Project project = loadProject(permission);
        return switch (project.getStatus()) {
            case IN_PROGRESS, COMPLETED -> true;
            case DRAFT -> ProjectRoleHelper.isOwner(subject, project);
            case PENDING_REVIEW, ABORTED ->
                ProjectRoleHelper.isOwner(subject, project) || ProjectRoleHelper.isCentralCore(subject);
        };
    }

    /** PLAN 파트 챌린저는 신규 프로젝트 작성 가능. */
    private boolean canWrite(SubjectAttributes subject) {
        return subject.gisuChallengerInfos().stream()
            .anyMatch(info -> info.part() == ChallengerPart.PLAN);
    }

    /**
     * 단건 EDIT 분기 — 정책 A (작성자 + Central Core).
     * <p>
     * COMPLETED / ABORTED 는 종료 상태라 절대 차단 (도메인 레벨 가드 {@code Project#validateMutable} 와 정합).
     */
    private boolean canEdit(SubjectAttributes subject, ResourcePermission permission) {
        Project project = loadProject(permission);
        if (ProjectRoleHelper.isOwner(subject, project)) {
            return switch (project.getStatus()) {
                case DRAFT, PENDING_REVIEW, IN_PROGRESS -> true;
                case COMPLETED, ABORTED -> false;
            };
        }
        return switch (project.getStatus()) {
            case PENDING_REVIEW, IN_PROGRESS -> ProjectRoleHelper.isCentralCore(subject);
            case DRAFT, COMPLETED, ABORTED -> false;
        };
    }

    private Project loadProject(ResourcePermission permission) {
        Long projectId = permission.getResourceIdAsLong();
        return loadProjectPort.findById(projectId)
            .orElseThrow(() -> new ProjectDomainException(ProjectErrorCode.PROJECT_NOT_FOUND));
    }
}
