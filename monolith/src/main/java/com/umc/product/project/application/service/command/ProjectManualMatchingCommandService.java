package com.umc.product.project.application.service.command;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
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
import com.umc.product.project.application.service.policy.ProjectMatchingAdjustmentPolicy;
import com.umc.product.project.application.service.policy.ProjectMatchingChapterResolver;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectApplication;
import com.umc.product.project.domain.ProjectMatchingRound;
import com.umc.product.project.domain.ProjectMember;
import com.umc.product.project.domain.enums.MatchingType;
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
    private final ProjectMatchingAdjustmentPolicy adjustmentPolicy;

    @Override
    public Long assign(AddProjectMemberCommand command) {
        LockedContext context = lockProject(command.projectId(), command.requesterMemberId());
        Project project = context.project();
        validateMatchingPart(command.part());

        MatchingType matchingType = MatchingType.fromPart(command.part()).orElseThrow();
        ProjectMatchingRound adjustmentRound = adjustmentPolicy.validateChange(
            context.rounds(), matchingType, Instant.now()).orElse(null);
        Long adjustmentRoundId = adjustmentRound == null ? null : adjustmentRound.getId();
        project.validateApplicable();
        validateCandidate(project, command.memberId(), command.part(), adjustmentRoundId);
        validateQuota(project, command.part(), adjustmentRoundId);

        ProjectMember member = loadProjectMemberPort
            .findByProjectIdAndMemberIdForUpdate(project.getId(), command.memberId()).orElse(null);
        if (member == null) {
            member = adjustmentRound == null
                ? ProjectMember.create(project, command.memberId(), command.part(), command.requesterMemberId())
                : ProjectMember.createForMatching(project, command.memberId(), command.part(), adjustmentRound,
                    command.requesterMemberId());
        } else if (adjustmentRound != null && adjustmentRoundId.equals(member.getPendingRemovalRoundId())) {
            // 같은 팀에 다시 배정하면 해제 예약만 취소하고 기존 지원서와 선발 차수를 유지한다.
            if (member.getPart() != command.part()) {
                throw new ProjectDomainException(ProjectErrorCode.PROJECT_APPLICATION_PART_NOT_ALLOWED);
            }
            member.cancelRemoval(adjustmentRound, command.requesterMemberId());
        } else if (adjustmentRound != null) {
            member.assignForAdjustment(command.part(), adjustmentRound, command.requesterMemberId());
        } else {
            member.assignManually(command.part(), command.requesterMemberId());
        }
        return saveProjectMemberPort.save(member).getId();
    }

    @Override
    public void unassign(RemoveProjectMemberCommand command) {
        LockedContext context = lockProject(command.projectId(), command.requesterMemberId());
        Project project = context.project();
        ProjectMember member = getMatchingMember(project, command.memberId());

        MatchingType matchingType = MatchingType.fromPart(member.getPart()).orElseThrow();
        ProjectMatchingRound adjustmentRound = adjustmentPolicy.validateChange(
            context.rounds(), matchingType, Instant.now()).orElse(null);
        if (adjustmentRound != null && member.getStatus() == ProjectMemberStatus.ACTIVE) {
            // 공개된 팀원은 최종 결과 공개 전까지 기존 명단에 남아 있어야 하므로 해제만 예약한다.
            member.scheduleRemoval(adjustmentRound, command.requesterMemberId(), command.reason());
            saveProjectMemberPort.save(member);
            log.info("프로젝트 매칭 해제 예약: projectId={}, memberId={}, matchingRoundId={}, requesterMemberId={}, reason={}",
                project.getId(), command.memberId(), adjustmentRound.getId(), command.requesterMemberId(), command.reason());
            return;
        }

        // 조정 기간의 PENDING_PUBLICATION 배정 취소와 그 외 허용 기간의 해제는 즉시 반영한다.
        rejectLinkedApplication(member, command.requesterMemberId(), command.reason());
        log.info("프로젝트 매칭 해제: projectId={}, memberId={}, requesterMemberId={}, reason={}",
            project.getId(), command.memberId(), command.requesterMemberId(), command.reason());
        saveProjectMemberPort.hardDelete(member.getId());
    }

    @Override
    public void changeStatus(ChangeProjectMemberStatusCommand command) {
        Project project = lockProject(command.projectId(), command.requesterMemberId()).project();
        ProjectMember member = getMatchingMember(project, command.memberId());
        if (!member.changeStatus(command.status(), command.requesterMemberId(), command.reason())) {
            return;
        }
        if (command.status() == ProjectMemberStatus.WITHDRAWN || command.status() == ProjectMemberStatus.DISMISSED) {
            rejectLinkedApplication(member, command.requesterMemberId(), command.reason());
        }
        saveProjectMemberPort.save(member);
    }

    private LockedContext lockProject(Long projectId, Long requesterMemberId) {
        lockProjectMatchingPort.lockByProjectId(projectId);
        Project snapshot = loadProjectPort.getById(projectId);
        accessPolicy.validate(requesterMemberId, snapshot);
        List<ProjectMatchingRound> rounds = loadProjectMatchingRoundPort.listByChapterIdForUpdate(snapshot.getChapterId());
        Project project = loadProjectPort.getByIdForUpdate(projectId);
        accessPolicy.validate(requesterMemberId, project);
        project.validateMutable();
        return new LockedContext(project, rounds);
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

    private void validateCandidate(Project project, Long memberId, ChallengerPart part, Long adjustmentRoundId) {
        validateMatchingPart(part);
        var challenger = getChallengerUseCase.getActiveByMemberIdAndGisuId(memberId, project.getGisuId());
        if (challenger.part() != part) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_APPLICATION_PART_NOT_ALLOWED);
        }
        if (!matchingChapterResolver.isInMatchingChapter(memberId, project.getGisuId(), project.getChapterId())) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_APPLICATION_CHAPTER_MISMATCH);
        }
        if (project.isOwner(memberId) || isAlreadyAssigned(project.getGisuId(), memberId, adjustmentRoundId)) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_APPLICATION_MEMBER_ALREADY_IN_TEAM);
        }
    }

    private boolean isAlreadyAssigned(Long gisuId, Long memberId, Long adjustmentRoundId) {
        return loadProjectMemberPort.existsAssignedMemberExcludingPendingRemoval(gisuId, memberId, adjustmentRoundId)
            || loadProjectApplicationPort.existsApprovedApplicationExcludingPendingRemoval(gisuId, memberId, adjustmentRoundId);
    }

    private void validateMatchingPart(ChallengerPart part) {
        if (part == null || part == ChallengerPart.PLAN || part == ChallengerPart.ADMIN) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_APPLICATION_PART_NOT_ALLOWED);
        }
    }

    private void validateQuota(Project project, ChallengerPart part, Long adjustmentRoundId) {
        long quota = loadProjectPartQuotaPort.listByProjectId(project.getId()).stream()
            .filter(q -> q.getPart() == part).mapToLong(q -> q.getQuota()).findFirst().orElse(0L);
        long assigned = loadProjectMemberPort.countAssignedByProjectIdGroupByPart(project.getId(), adjustmentRoundId)
            .getOrDefault(part, 0L);
        if (assigned >= quota) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_MANUAL_MATCHING_QUOTA_EXCEEDED);
        }
        if (assigned + countUnassignedApprovedApplicants(project, part) >= quota) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_MANUAL_MATCHING_QUOTA_RESERVED_BY_APPROVAL);
        }
    }

    /**
     * 해당 프로젝트·파트의 공개 전 일반 차수 APPROVED 지원자 중 아직 팀원으로 반영되지 않은 인원을 계산한다.
     * ACTIVE 또는 PENDING_PUBLICATION 팀원의 지원서는 제외하며, 해제 예약자의 지원서도 TO를 다시 차감하지 않는다.
     */
    private long countUnassignedApprovedApplicants(Project project, ChallengerPart part) {
        Instant now = Instant.now();
        MatchingType type = MatchingType.fromPart(part).orElseThrow();
        Set<Long> approvedMemberIds = loadProjectApplicationPort.searchProjectApplications(
                project.getId(), null, ProjectApplicationStatus.APPROVED, now, true).stream()
            .filter(application -> application.getAppliedMatchingRound().getType() == type)
            .filter(application -> !application.getAppliedMatchingRound().isRandomMatching())
            .filter(application -> !application.getAppliedMatchingRound().isResultPublishedAt(now))
            .map(ProjectApplication::getApplicantMemberId)
            .collect(Collectors.toSet());
        if (approvedMemberIds.isEmpty()) {
            return 0;
        }

        Set<Long> assignedMemberIds = loadProjectMemberPort.listAssignedMemberIdsByProjectIds(List.of(project.getId()))
            .getOrDefault(project.getId(), Set.of());
        approvedMemberIds.removeAll(assignedMemberIds);
        if (approvedMemberIds.isEmpty()) {
            return 0;
        }

        Map<Long, ChallengerInfo> challengers = getChallengerUseCase
            .batchGetByMemberIdsAndGisuId(approvedMemberIds, project.getGisuId());
        return approvedMemberIds.stream().filter(memberId -> challengers.get(memberId).part() == part).count();
    }

    private void rejectLinkedApplication(ProjectMember member, Long requesterMemberId, String reason) {
        if (member.getApplication() != null) {
            member.getApplication().rejectForMatchingRelease(requesterMemberId, reason);
            saveProjectApplicationPort.save(member.getApplication());
        }
    }

    private record LockedContext(Project project, List<ProjectMatchingRound> rounds) {}
}
