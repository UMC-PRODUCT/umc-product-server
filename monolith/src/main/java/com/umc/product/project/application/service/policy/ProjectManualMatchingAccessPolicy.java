package com.umc.product.project.application.service.policy;

import java.util.Collection;

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
        AuthoritySnapshot authority = checkPermissionUseCase.loadSubject(requesterMemberId).toAuthoritySnapshot();
        validate(authority, project);
    }

    /** 요청자의 권한 정보를 한 번 조회하고, 모든 프로젝트에 조정 권한이 있는지 검증한다. */
    public void validateAll(Long requesterMemberId, Collection<Project> projects) {
        AuthoritySnapshot authority = checkPermissionUseCase.loadSubject(requesterMemberId).toAuthoritySnapshot();
        projects.forEach(project -> validate(authority, project));
    }

    private void validate(AuthoritySnapshot authority, Project project) {
        if (!canManage(authority, project)) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_ACCESS_DENIED);
        }
    }
}
