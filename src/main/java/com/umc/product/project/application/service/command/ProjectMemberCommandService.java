package com.umc.product.project.application.service.command;

import com.umc.product.project.application.port.in.command.AddProjectMemberUseCase;
import com.umc.product.project.application.port.in.command.dto.AddProjectMemberCommand;
import com.umc.product.project.application.port.out.LoadProjectMemberPort;
import com.umc.product.project.application.port.out.LoadProjectPort;
import com.umc.product.project.application.port.out.SaveProjectMemberPort;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectMember;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 프로젝트 멤버 관리 Command 서비스 (PROJECT-004/005).
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ProjectMemberCommandService implements AddProjectMemberUseCase {

    private final LoadProjectPort loadProjectPort;
    private final LoadProjectMemberPort loadProjectMemberPort;
    private final SaveProjectMemberPort saveProjectMemberPort;

    @Override
    public Long add(AddProjectMemberCommand command) {
        Project project = loadProjectPort.getById(command.projectId());

        // L5 도메인 가드 — COMPLETED/ABORTED 차단
        project.validateMutable();

        // 동일 (projectId, memberId) row 존재 시 차단 (status 무관 — uk_project_member_project_member)
        loadProjectMemberPort.findByProjectIdAndMemberId(command.projectId(), command.memberId())
            .ifPresent(existing -> {
                throw new ProjectDomainException(ProjectErrorCode.PROJECT_MEMBER_ALREADY_EXISTS);
            });

        ProjectMember member = ProjectMember.create(
            project, command.memberId(), command.part(), command.requesterMemberId());
        return saveProjectMemberPort.save(member).getId();
    }
}
