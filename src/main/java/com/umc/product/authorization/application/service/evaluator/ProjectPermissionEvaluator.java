package com.umc.product.authorization.application.service.evaluator;

import com.umc.product.authorization.application.port.out.ResourcePermissionEvaluator;
import com.umc.product.authorization.domain.ResourcePermission;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.authorization.domain.SubjectAttributes;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.application.port.out.LoadProjectPort;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.enums.ProjectStatus;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectPermissionEvaluator implements ResourcePermissionEvaluator {

    private final LoadProjectPort loadProjectPort;

    @Override
    public ResourceType supportedResourceType() {
        return ResourceType.PROJECT;
    }

    @Override
    public boolean evaluate(SubjectAttributes subjectAttributes, ResourcePermission resourcePermission) {
        return switch (resourcePermission.permission()) {
            case READ -> true;
            case WRITE -> canWrite(subjectAttributes);
            case EDIT -> canEdit(subjectAttributes, resourcePermission);
            case DELETE -> isCentralCore(subjectAttributes);
            default -> false;
        };
    }

    private boolean canWrite(SubjectAttributes subjectAttributes) {
        return subjectAttributes.gisuChallengerInfos().stream()
            .anyMatch(info -> info.part() == ChallengerPart.PLAN);
    }

    private boolean canEdit(SubjectAttributes subjectAttributes, ResourcePermission resourcePermission) {
        Long projectId = resourcePermission.getResourceIdAsLong();
        Project project = loadProjectPort.findById(projectId)
            .orElseThrow(() -> new ProjectDomainException(ProjectErrorCode.PROJECT_NOT_FOUND));

        boolean isOwner = project.getProductOwnerMemberId().equals(subjectAttributes.memberId());

        if (project.getStatus() == ProjectStatus.DRAFT) {
            return isOwner;
        }

        if (project.getStatus() == ProjectStatus.PENDING_REVIEW
            || project.getStatus() == ProjectStatus.IN_PROGRESS) {
            return isOwner || isCentralCore(subjectAttributes);
        }

        return false;
    }

    private boolean isCentralCore(SubjectAttributes subjectAttributes) {
        return subjectAttributes.roleAttributes().stream()
            .anyMatch(role -> role.roleType().isAtLeastCentralCore());
    }
}
