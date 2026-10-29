package com.umc.product.project.application.service.command;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.audit.application.port.in.annotation.Audited;
import com.umc.product.audit.domain.AuditAction;
import com.umc.product.authorization.application.port.in.CheckPermissionUseCase;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourcePermission;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.project.application.port.in.command.UpdatePartQuotasUseCase;
import com.umc.product.project.application.port.in.command.dto.UpdatePartQuotasCommand;
import com.umc.product.project.application.port.in.command.dto.UpdatePartQuotasCommand.Entry;
import com.umc.product.project.application.port.out.LoadProjectMatchingRoundPort;
import com.umc.product.project.application.port.out.LoadProjectMemberPort;
import com.umc.product.project.application.port.out.LoadProjectPartQuotaPort;
import com.umc.product.project.application.port.out.LoadProjectPort;
import com.umc.product.project.application.port.out.LockProjectMatchingPort;
import com.umc.product.project.application.port.out.SaveProjectPartQuotaPort;
import com.umc.product.project.application.service.policy.ProjectDraftQuotaPolicy;
import com.umc.product.project.application.service.policy.ProjectQuotaCapacityPolicy;
import com.umc.product.project.application.service.policy.ProjectQuotaEditPolicy;
import com.umc.product.project.application.service.policy.ProjectQuotaMemberPolicy;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectMember;
import com.umc.product.project.domain.ProjectPartQuota;
import com.umc.product.project.domain.enums.ProjectStatus;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectPartQuotaCommandService implements UpdatePartQuotasUseCase {

    private final ProjectDraftQuotaPolicy draftQuotaPolicy;
    private final Clock clock;
    private final CheckPermissionUseCase checkPermissionUseCase;
    private final LockProjectMatchingPort lockProjectMatchingPort;
    private final LoadProjectMatchingRoundPort loadProjectMatchingRoundPort;
    private final LoadProjectMemberPort loadProjectMemberPort;
    private final ProjectQuotaEditPolicy editPolicy;
    private final ProjectQuotaMemberPolicy memberPolicy;
    private final ProjectQuotaCapacityPolicy capacityPolicy;
    private final LoadProjectPort loadProjectPort;
    private final LoadProjectPartQuotaPort loadProjectPartQuotaPort;
    private final SaveProjectPartQuotaPort saveProjectPartQuotaPort;

    /**
     * PUT 시멘틱 — 본문에 있는 파트만 유지/갱신, 본문에 없는 기존 파트는 삭제.
     */
    @Audited(
        domain = Domain.PROJECT,
        action = AuditAction.UPDATE,
        targetType = "ProjectPartQuota",
        targetId = "#command.projectId()",
        description = "'프로젝트 파트 정원을 수정했습니다.'"
    )
    @Override
    public void update(UpdatePartQuotasCommand command) {
        updateWithinBatch(command);
        Project project = loadProjectPort.getById(command.projectId());
        capacityPolicy.validate(project.getGisuId(), project.getChapterId());
    }

    // 일괄 저장은 모든 프로젝트를 갱신한 뒤 지부 TO 상한을 검증한다.
    void updateWithinBatch(UpdatePartQuotasCommand command) {
        lockProjectMatchingPort.lockByProjectId(command.projectId());
        Project target = loadProjectPort.getById(command.projectId());
        Long chapterId = target.getChapterId();
        var rounds = loadProjectMatchingRoundPort.listByChapterIdForUpdate(chapterId);
        Project project = loadProjectPort.getByIdForUpdate(command.projectId());
        if (!Objects.equals(chapterId, project.getChapterId())) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_INVALID_STATE);
        }
        if (project.getStatus() == ProjectStatus.DRAFT) {
            if (!draftQuotaPolicy.canEdit(project, checkPermissionUseCase.loadSubject(command.requesterMemberId()))) {
                throw new ProjectDomainException(ProjectErrorCode.PROJECT_ACCESS_DENIED);
            }
        } else {
            checkPermissionUseCase.checkOrThrow(command.requesterMemberId(),
                ResourcePermission.of(ResourceType.PROJECT, project.getId(), PermissionType.MANAGE));
        }
        project.validateMutable();

        validateEntries(command.entries());

        List<ProjectPartQuota> existing = loadProjectPartQuotaPort.listByProjectId(command.projectId());
        Map<ChallengerPart, ProjectPartQuota> existingByPart = existing.stream()
            .collect(Collectors.toMap(ProjectPartQuota::getPart, q -> q));

        Set<ChallengerPart> requestedParts = command.entries().stream()
            .map(Entry::part).collect(Collectors.toSet());

        Set<ChallengerPart> partsToDelete = existingByPart.keySet().stream()
            .filter(p -> !requestedParts.contains(p))
            .collect(Collectors.toSet());

        Instant now = clock.instant();
        var reserved = memberPolicy.reservedMembers(
            loadProjectMemberPort.listActiveAndPendingPublicationByProjectId(project.getId()), rounds, now);
        Map<ChallengerPart, Long> assigned = reserved.stream()
            .collect(Collectors.groupingBy(ProjectMember::getPart, Collectors.counting()));
        Map<ChallengerPart, Long> requested = command.entries().stream()
            .collect(Collectors.toMap(Entry::part, Entry::quota));
        Set<ChallengerPart> changed = EnumSet.noneOf(ChallengerPart.class);
        changed.addAll(existingByPart.keySet());
        changed.addAll(requested.keySet());
        for (ChallengerPart part : changed) {
            long minimum = assigned.getOrDefault(part, 0L);
            if (part == ChallengerPart.PLAN) {
                var ids = new HashSet<Long>();
                ids.add(project.getProductOwnerMemberId());
                reserved.stream()
                    .filter(member -> member.getPart() == ChallengerPart.PLAN)
                    .map(ProjectMember::getMemberId).forEach(ids::add);
                minimum = ids.size();
            }
            long defaultQuota = part == ChallengerPart.PLAN ? minimum : 0;
            long before = existingByPart.containsKey(part) ? existingByPart.get(part).getQuota() : defaultQuota;
            long after = requested.getOrDefault(part, 0L);
            if (before == after) {
                continue;
            }
            if (part == ChallengerPart.ADMIN) {
                throw new ProjectDomainException(ProjectErrorCode.PROJECT_PART_QUOTA_READ_ONLY);
            }
            editPolicy.validate(part, rounds, now);
            if (after < minimum) {
                throw new ProjectDomainException(ProjectErrorCode.PROJECT_PART_QUOTA_BELOW_ASSIGNED,
                    part.getDisplayName() + ": 이미 배정된 인원보다 TO를 줄일 수 없어요.");
            }
        }

        if (!partsToDelete.isEmpty()) {
            saveProjectPartQuotaPort.deleteByProjectIdAndPartIn(command.projectId(), partsToDelete);
        }

        List<ProjectPartQuota> toSave = new ArrayList<>();
        for (Entry entry : command.entries()) {
            ProjectPartQuota existingQuota = existingByPart.get(entry.part());
            if (existingQuota != null) {
                existingQuota.updateQuota(entry.quota(), command.requesterMemberId());
                toSave.add(existingQuota);
            } else {
                toSave.add(ProjectPartQuota.create(
                    project, entry.part(), entry.quota(), command.requesterMemberId()));
            }
        }
        saveProjectPartQuotaPort.saveAll(toSave);
    }

    private void validateEntries(List<Entry> entries) {
        Set<ChallengerPart> seen = EnumSet.noneOf(ChallengerPart.class);
        for (Entry entry : entries) {
            if (entry.quota() < 1) {
                throw new ProjectDomainException(ProjectErrorCode.PROJECT_PART_QUOTA_INVALID);
            }
            if (!seen.add(entry.part())) {
                throw new ProjectDomainException(ProjectErrorCode.PROJECT_PART_QUOTA_DUPLICATE);
            }
        }
        if (seen.contains(ChallengerPart.WEB_PRODUCT_ENGINEER)
            && seen.contains(ChallengerPart.MOBILE_PRODUCT_ENGINEER)) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_DEVELOPER_PART_QUOTA_CONFLICT);
        }
    }
}
