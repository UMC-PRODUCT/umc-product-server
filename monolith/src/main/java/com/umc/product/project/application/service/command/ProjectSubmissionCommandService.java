package com.umc.product.project.application.service.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.authorization.application.port.in.CheckPermissionUseCase;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourcePermission;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.project.application.port.in.command.PublishProjectUseCase;
import com.umc.product.project.application.port.in.command.SubmitProjectUseCase;
import com.umc.product.project.application.port.in.command.dto.PublishProjectCommand;
import com.umc.product.project.application.port.in.command.dto.SubmitProjectCommand;
import com.umc.product.project.application.port.out.LoadProjectApplicationFormPort;
import com.umc.product.project.application.port.out.LoadProjectPort;
import com.umc.product.project.application.port.out.LockProjectMatchingPort;
import com.umc.product.project.application.service.policy.ProjectQuotaCapacityPolicy;
import com.umc.product.project.domain.enums.ProjectStatus;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectSubmissionCommandService implements SubmitProjectUseCase {
    private final LockProjectMatchingPort lockProjectMatchingPort;
    private final ProjectQuotaCapacityPolicy capacityPolicy;
    private final LoadProjectPort loadProjectPort;
    private final LoadProjectApplicationFormPort loadProjectApplicationFormPort;
    private final CheckPermissionUseCase checkPermissionUseCase;
    private final PublishProjectUseCase publishProjectUseCase;

    @Override
    public ProjectStatus submit(SubmitProjectCommand command) {
        lockProjectMatchingPort.lockByProjectId(command.projectId());
        var project = loadProjectPort.getByIdForUpdate(command.projectId());
        checkPermissionUseCase.checkOrThrow(command.requesterMemberId(),
            ResourcePermission.of(ResourceType.PROJECT, project.getId(), PermissionType.EDIT));
        if (!loadProjectApplicationFormPort.existsByProjectId(project.getId())) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_SUBMIT_VALIDATION_FAILED);
        }

        project.submit();
        // 승인대기 상태에서 기존 공개 API와 동일한 기수·지부 관리 권한을 평가한다.
        boolean canPublish = checkPermissionUseCase.check(command.requesterMemberId(),
            ResourcePermission.of(ResourceType.PROJECT, project.getId(), PermissionType.MANAGE));
        if (canPublish) {
            ProjectStatus status = publishProjectUseCase.publish(PublishProjectCommand.builder()
                .projectId(project.getId()).requesterMemberId(command.requesterMemberId()).build());
            capacityPolicy.validate(project.getGisuId(), project.getChapterId());
            return status;
        }
        if (!project.isOwner(command.requesterMemberId())) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_ACCESS_DENIED);
        }
        return project.getStatus();
    }
}
