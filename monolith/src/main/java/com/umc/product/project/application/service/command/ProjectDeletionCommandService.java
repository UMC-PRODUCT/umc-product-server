package com.umc.product.project.application.service.command;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.audit.application.port.in.annotation.Audited;
import com.umc.product.audit.domain.AuditAction;
import com.umc.product.authorization.application.port.in.CheckPermissionUseCase;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourcePermission;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.form.application.port.in.command.ManageFormUseCase;
import com.umc.product.form.application.port.in.command.dto.DeleteFormCommand;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.project.application.port.in.command.DeleteProjectUseCase;
import com.umc.product.project.application.port.in.command.dto.DeleteProjectCommand;
import com.umc.product.project.application.port.out.LoadProjectApplicationFormPort;
import com.umc.product.project.application.port.out.LoadProjectMatchingRoundPort;
import com.umc.product.project.application.port.out.LoadProjectPort;
import com.umc.product.project.application.port.out.SaveProjectApplicationFormPolicyPort;
import com.umc.product.project.application.port.out.SaveProjectApplicationFormPort;
import com.umc.product.project.application.port.out.SaveProjectApplicationPort;
import com.umc.product.project.application.port.out.SaveProjectMemberPort;
import com.umc.product.project.application.port.out.SaveProjectPartQuotaPort;
import com.umc.product.project.application.port.out.SaveProjectPort;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectApplicationForm;
import com.umc.product.project.domain.ProjectMatchingRound;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectDeletionCommandService implements DeleteProjectUseCase {

    private final LoadProjectPort loadProjectPort;
    private final LoadProjectMatchingRoundPort loadProjectMatchingRoundPort;
    private final LoadProjectApplicationFormPort loadProjectApplicationFormPort;
    private final SaveProjectApplicationPort saveProjectApplicationPort;
    private final SaveProjectMemberPort saveProjectMemberPort;
    private final SaveProjectPartQuotaPort saveProjectPartQuotaPort;
    private final SaveProjectApplicationFormPolicyPort saveProjectApplicationFormPolicyPort;
    private final SaveProjectApplicationFormPort saveProjectApplicationFormPort;
    private final SaveProjectPort saveProjectPort;
    private final ManageFormUseCase manageFormUseCase;
    private final CheckPermissionUseCase checkPermissionUseCase;

    @Audited(
        domain = Domain.PROJECT,
        action = AuditAction.DELETE,
        targetType = "Project",
        targetId = "#command.projectId()",
        description = "'프로젝트를 삭제했습니다.'"
    )
    @Override
    public void delete(DeleteProjectCommand command) {
        Project target = loadProjectPort.getById(command.projectId());
        checkPermissionUseCase.checkOrThrow(command.requesterMemberId(),
            ResourcePermission.of(ResourceType.PROJECT, target.getId(), PermissionType.DELETE));

        // 자동 선발·결과 발표도 같은 차수를 잠근다. 해당 처리가 끝난 뒤 배정과 지원서를 삭제한다.
        List<ProjectMatchingRound> rounds =
            loadProjectMatchingRoundPort.listByChapterIdForUpdate(target.getChapterId());
        Long chapterId = target.getChapterId();
        Project project = loadProjectPort.getByIdForUpdate(command.projectId());
        if (!Objects.equals(chapterId, project.getChapterId())) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_INVALID_STATE);
        }
        checkPermissionUseCase.checkOrThrow(command.requesterMemberId(),
            ResourcePermission.of(ResourceType.PROJECT, project.getId(), PermissionType.DELETE));
        Instant now = Instant.now();
        project.validateDeletable(rounds.stream().anyMatch(round -> round.isOpenAt(now)));

        List<ProjectApplicationForm> forms = loadProjectApplicationFormPort.listByProjectId(project.getId());
        // 팀원의 지원서 참조를 먼저 정리한다. 다른 프로젝트의 배정은 변경하지 않는다.
        saveProjectMemberPort.deleteAllByProjectId(project.getId());
        saveProjectApplicationPort.deleteAllByProjectId(project.getId());
        for (ProjectApplicationForm form : forms) {
            saveProjectApplicationFormPolicyPort.deleteAllByApplicationFormId(form.getId());
        }
        saveProjectApplicationFormPort.deleteAllByProjectId(project.getId());
        for (ProjectApplicationForm form : forms) {
            manageFormUseCase.deleteForm(DeleteFormCommand.builder()
                .formId(form.getFormId())
                .requesterMemberId(command.requesterMemberId())
                .build());
        }
        saveProjectPartQuotaPort.deleteAllByProjectId(project.getId());
        saveProjectPort.delete(project);
    }
}
