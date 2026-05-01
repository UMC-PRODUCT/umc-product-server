package com.umc.product.project.application.service.command;

import com.umc.product.project.application.port.in.command.AddProjectMemberUseCase;
import com.umc.product.project.application.port.in.command.RemoveProjectMemberUseCase;
import com.umc.product.project.application.port.in.command.dto.AddProjectMemberCommand;
import com.umc.product.project.application.port.in.command.dto.RemoveProjectMemberCommand;
import com.umc.product.project.application.port.out.LoadProjectMemberPort;
import com.umc.product.project.application.port.out.LoadProjectPort;
import com.umc.product.project.application.port.out.SaveProjectMemberPort;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectMember;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectMemberCommandService implements AddProjectMemberUseCase, RemoveProjectMemberUseCase {

    private final LoadProjectPort loadProjectPort;
    private final LoadProjectMemberPort loadProjectMemberPort;
    private final SaveProjectMemberPort saveProjectMemberPort;

    @Override
    public Long add(AddProjectMemberCommand command) {
        Project project = loadProjectPort.getById(command.projectId());
        project.validateMutable();

        loadProjectMemberPort.findByProjectIdAndMemberId(command.projectId(), command.memberId())
            .ifPresent(existing -> {
                throw new ProjectDomainException(ProjectErrorCode.PROJECT_MEMBER_ALREADY_EXISTS);
            });

        ProjectMember member = ProjectMember.create(
            project, command.memberId(), command.part(), command.requesterMemberId());
        return saveProjectMemberPort.save(member).getId();
    }

    /**
     * 멤버 제거. status 에 따라 hard / soft delete 가 갈린다.
     * <ul>
     *   <li>DRAFT / PENDING_REVIEW: hard delete (실수 정정)</li>
     *   <li>IN_PROGRESS: soft delete (status = DISMISSED, 매칭/출석 등 외부 도메인 무결성)</li>
     *   <li>COMPLETED / ABORTED: 거부</li>
     * </ul>
     * 메인 PM 은 양도 API 로 변경해야 하므로 본 메서드에서 거부한다.
     */
    @Override
    public void remove(RemoveProjectMemberCommand command) {
        Project project = loadProjectPort.getById(command.projectId());

        if (Objects.equals(project.getProductOwnerMemberId(), command.memberId())) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_MAIN_PM_REMOVAL_REQUIRES_TRANSFER);
        }

        ProjectMember member = loadProjectMemberPort
            .findByProjectIdAndMemberId(command.projectId(), command.memberId())
            .orElseThrow(() -> new ProjectDomainException(ProjectErrorCode.PROJECT_MEMBER_NOT_FOUND));

        switch (project.getStatus()) {
            case DRAFT, PENDING_REVIEW -> saveProjectMemberPort.hardDelete(member.getId());
            case IN_PROGRESS -> {
                member.dismiss(command.reason(), command.requesterMemberId());
                saveProjectMemberPort.save(member);
            }
            case COMPLETED, ABORTED ->
                throw new ProjectDomainException(ProjectErrorCode.PROJECT_INVALID_STATE);
        }
    }
}
