package com.umc.product.project.application.service.command;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.authorization.application.port.in.CheckPermissionUseCase;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourcePermission;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.project.application.port.in.command.UpdateProjectUseCase;
import com.umc.product.project.application.port.in.command.dto.UpdateProjectCommand;
import com.umc.product.project.application.port.out.LoadProjectMatchingRoundPort;
import com.umc.product.project.application.port.out.LoadProjectPort;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectMatchingRound;
import com.umc.product.project.domain.enums.ProjectStatus;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectUpdateCommandService implements UpdateProjectUseCase {
    private final LoadProjectPort loadProjectPort;
    private final LoadProjectMatchingRoundPort loadProjectMatchingRoundPort;
    private final CheckPermissionUseCase checkPermissionUseCase;
    private final Clock clock;

    @Override
    public ProjectStatus update(UpdateProjectCommand command) {
        Project target = loadProjectPort.getById(command.projectId());
        checkEditPermission(command.requesterMemberId(), target.getId());
        // 삭제·매칭 처리와 동일하게 차수를 프로젝트보다 먼저 잠근다.
        Long chapterId = target.getChapterId();
        List<ProjectMatchingRound> rounds = loadProjectMatchingRoundPort.listByChapterIdForUpdate(chapterId);
        Project project = loadProjectPort.getByIdForUpdate(command.projectId());
        if (!Objects.equals(chapterId, project.getChapterId())) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_INVALID_STATE);
        }
        checkEditPermission(command.requesterMemberId(), project.getId());
        project.validateMutable();
        Instant now = clock.instant();
        if (rounds.stream().anyMatch(round -> round.isOpenAt(now))) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_UPDATE_DURING_APPLICATION_PERIOD);
        }
        project.updateBasicInfo(command.name(), command.description(), command.externalLink(),
            command.thumbnailFileId(), command.logoFileId());
        return project.getStatus();
    }

    private void checkEditPermission(Long requesterId, Long projectId) {
        checkPermissionUseCase.checkOrThrow(requesterId,
            ResourcePermission.of(ResourceType.PROJECT, projectId, PermissionType.EDIT));
    }
}
