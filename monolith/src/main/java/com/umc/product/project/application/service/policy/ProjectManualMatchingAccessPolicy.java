package com.umc.product.project.application.service.policy;

import org.springframework.stereotype.Component;

import com.umc.product.authorization.application.port.in.CheckPermissionUseCase;
import com.umc.product.authorization.domain.AuthoritySnapshot;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProjectManualMatchingAccessPolicy {
    private final CheckPermissionUseCase checkPermissionUseCase;

    public static boolean canManage(AuthoritySnapshot authority, Project project) {
        return authority.isCentralCoreInGisu(project.getGisuId())
            || authority.isChapterPresidentInGisu(project.getGisuId(), project.getChapterId());
    }

    public void validate(Long requesterMemberId, Project project) {
        var authority = checkPermissionUseCase.loadSubject(requesterMemberId).toAuthoritySnapshot();
        if (!canManage(authority, project)) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_ACCESS_DENIED);
        }
    }
}
