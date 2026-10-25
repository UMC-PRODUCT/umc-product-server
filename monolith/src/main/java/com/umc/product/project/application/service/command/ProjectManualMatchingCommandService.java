package com.umc.product.project.application.service.command;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.application.port.in.command.ManageProjectMatchingUseCase;
import com.umc.product.project.application.port.in.command.dto.AddProjectMemberCommand;
import com.umc.product.project.application.port.in.command.dto.ChangeProjectMemberStatusCommand;
import com.umc.product.project.application.port.in.command.dto.RemoveProjectMemberCommand;
import com.umc.product.project.application.port.out.LoadProjectApplicationPort;
import com.umc.product.project.application.port.out.LoadProjectMatchingRoundPort;
import com.umc.product.project.application.port.out.LoadProjectMemberPort;
import com.umc.product.project.application.port.out.LoadProjectPartQuotaPort;
import com.umc.product.project.application.port.out.LoadProjectPort;
import com.umc.product.project.application.port.out.LockProjectMatchingPort;
import com.umc.product.project.application.port.out.SaveProjectApplicationPort;
import com.umc.product.project.application.port.out.SaveProjectMemberPort;
import com.umc.product.project.application.service.policy.ProjectManualMatchingAccessPolicy;
import com.umc.product.project.application.service.policy.ProjectMatchingChapterResolver;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectMember;
import com.umc.product.project.domain.enums.ProjectApplicationStatus;
import com.umc.product.project.domain.enums.ProjectMemberStatus;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ProjectManualMatchingCommandService implements ManageProjectMatchingUseCase {
    private final LoadProjectPort loadProjectPort;
    private final LoadProjectMemberPort loadProjectMemberPort;
    private final LoadProjectPartQuotaPort loadProjectPartQuotaPort;
    private final LoadProjectApplicationPort loadProjectApplicationPort;
    private final SaveProjectMemberPort saveProjectMemberPort;
    private final SaveProjectApplicationPort saveProjectApplicationPort;
    private final LoadProjectMatchingRoundPort loadProjectMatchingRoundPort;
    private final LockProjectMatchingPort lockProjectMatchingPort;
    private final GetChallengerUseCase getChallengerUseCase;
    private final ProjectMatchingChapterResolver matchingChapterResolver;
    private final ProjectManualMatchingAccessPolicy accessPolicy;

    @Override
    public Long assign(AddProjectMemberCommand command) {
        Project project = lockProject(command.projectId(), command.requesterMemberId());
        project.validateApplicable();
        validateCandidate(project, command.memberId(), command.part());
        validateQuota(project, command.part());
        ProjectMember member = loadProjectMemberPort
            .findByProjectIdAndMemberIdForUpdate(project.getId(), command.memberId()).orElse(null);
        if (member == null) {
            member = ProjectMember.create(project, command.memberId(), command.part(), command.requesterMemberId());
        } else {
            member.assignManually(command.part(), command.requesterMemberId());
        }
        return saveProjectMemberPort.save(member).getId();
    }

    @Override
    public void unassign(RemoveProjectMemberCommand command) {
        Project project = lockProject(command.projectId(), command.requesterMemberId());
        ProjectMember member = getMatchingMember(project, command.memberId());
        rejectLinkedApplication(member, command.requesterMemberId(), command.reason());
        log.info("프로젝트 매칭 해제: projectId={}, memberId={}, requesterMemberId={}, reason={}",
            project.getId(), command.memberId(), command.requesterMemberId(), command.reason());
        saveProjectMemberPort.hardDelete(member.getId());
    }

    @Override
    public void changeStatus(ChangeProjectMemberStatusCommand command) {
        Project project = lockProject(command.projectId(), command.requesterMemberId());
        ProjectMember member = getMatchingMember(project, command.memberId());
        if (command.status() == ProjectMemberStatus.ACTIVE) {
            if (member.getStatus() == ProjectMemberStatus.ACTIVE) {
                return;
            }
            project.validateApplicable();
            validateCandidate(project, command.memberId(), member.getPart());
            validateQuota(project, member.getPart());
            member.assignManually(member.getPart(), command.requesterMemberId());
        } else {
            // PENDING_PUBLICATION으로 직접 변경하는 요청은 도메인에서 거부한다.
            member.changeStatus(command.status(), command.requesterMemberId(), command.reason());
            if (command.status() == ProjectMemberStatus.WITHDRAWN || command.status() == ProjectMemberStatus.DISMISSED) {
                rejectLinkedApplication(member, command.requesterMemberId(), command.reason());
            }
        }
        saveProjectMemberPort.save(member);
    }

    private Project lockProject(Long projectId, Long requesterMemberId) {
        lockProjectMatchingPort.lockByProjectId(projectId);
        Project snapshot = loadProjectPort.getById(projectId);
        accessPolicy.validate(requesterMemberId, snapshot);
        loadProjectMatchingRoundPort.listByChapterIdForUpdate(snapshot.getChapterId());
        Project project = loadProjectPort.getByIdForUpdate(projectId);
        accessPolicy.validate(requesterMemberId, project);
        project.validateMutable();
        return project;
    }

    private ProjectMember getMatchingMember(Project project, Long memberId) {
        if (project.isOwner(memberId)) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_MAIN_PM_REMOVAL_REQUIRES_TRANSFER);
        }
        ProjectMember member = loadProjectMemberPort.findByProjectIdAndMemberIdForUpdate(project.getId(), memberId)
            .orElseThrow(() -> new ProjectDomainException(ProjectErrorCode.PROJECT_MEMBER_NOT_FOUND));
        validateMatchingPart(member.getPart());
        return member;
    }

    private void validateCandidate(Project project, Long memberId, ChallengerPart part) {
        validateMatchingPart(part);
        var challenger = getChallengerUseCase.getActiveByMemberIdAndGisuId(memberId, project.getGisuId());
        if (challenger.part() != part) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_APPLICATION_PART_NOT_ALLOWED);
        }
        if (!matchingChapterResolver.isInMatchingChapter(memberId, project.getGisuId(), project.getChapterId())) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_APPLICATION_CHAPTER_MISMATCH);
        }
        if (project.isOwner(memberId)
            || loadProjectMemberPort.existsByGisuAndMember(project.getGisuId(), memberId)
            || !loadProjectApplicationPort.listByApplicantMemberIdAndGisuIdAndStatusIn(
                memberId, project.getGisuId(), List.of(ProjectApplicationStatus.APPROVED)).isEmpty()) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_APPLICATION_MEMBER_ALREADY_IN_TEAM);
        }
    }

    private void validateMatchingPart(ChallengerPart part) {
        if (part == null || part == ChallengerPart.PLAN || part == ChallengerPart.ADMIN) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_APPLICATION_PART_NOT_ALLOWED);
        }
    }

    private void validateQuota(Project project, ChallengerPart part) {
        long quota = loadProjectPartQuotaPort.listByProjectId(project.getId()).stream()
            .filter(q -> q.getPart() == part).mapToLong(q -> q.getQuota()).findFirst().orElse(0L);
        long assigned = loadProjectMemberPort.countAssignedByProjectIdGroupByPart(project.getId())
            .getOrDefault(part, 0L);
        if (assigned >= quota) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_APPLICATION_QUOTA_EXCEEDED);
        }
    }

    private void rejectLinkedApplication(ProjectMember member, Long requesterMemberId, String reason) {
        if (member.getApplication() != null) {
            member.getApplication().rejectForMatchingRelease(requesterMemberId, reason);
            saveProjectApplicationPort.save(member.getApplication());
        }
    }
}
