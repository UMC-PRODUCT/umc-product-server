package com.umc.product.project.application.service.command;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
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
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.project.application.access.ProjectQuotaAccessPolicy;
import com.umc.product.project.application.port.in.command.UpdatePartQuotasUseCase;
import com.umc.product.project.application.port.in.command.dto.UpdatePartQuotasCommand;
import com.umc.product.project.application.port.in.command.dto.UpdatePartQuotasCommand.Entry;
import com.umc.product.project.application.port.out.LoadProjectApplicationPort;
import com.umc.product.project.application.port.out.LoadProjectMatchingRoundPort;
import com.umc.product.project.application.port.out.LoadProjectMemberPort;
import com.umc.product.project.application.port.out.LoadProjectPartQuotaPort;
import com.umc.product.project.application.port.out.LoadProjectPort;
import com.umc.product.project.application.port.out.LockProjectMatchingPort;
import com.umc.product.project.application.port.out.SaveProjectPartQuotaPort;
import com.umc.product.project.application.service.policy.ProjectDraftQuotaPolicy;
import com.umc.product.project.application.service.policy.ProjectQuotaEditPolicy;
import com.umc.product.project.application.service.policy.ProjectQuotaMemberPolicy;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectApplication;
import com.umc.product.project.domain.ProjectMatchingRound;
import com.umc.product.project.domain.ProjectMember;
import com.umc.product.project.domain.ProjectPartQuota;
import com.umc.product.project.domain.enums.ProjectApplicationStatus;
import com.umc.product.project.domain.enums.ProjectStatus;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectPartQuotaCommandService implements UpdatePartQuotasUseCase {

    private final ProjectDraftQuotaPolicy draftQuotaPolicy;
    private final ProjectQuotaAccessPolicy quotaAccessPolicy;
    private final Clock clock;
    private final CheckPermissionUseCase checkPermissionUseCase;
    private final LockProjectMatchingPort lockProjectMatchingPort;
    private final LoadProjectMatchingRoundPort loadProjectMatchingRoundPort;
    private final LoadProjectMemberPort loadProjectMemberPort;
    private final LoadProjectApplicationPort loadProjectApplicationPort;
    private final GetChallengerUseCase getChallengerUseCase;
    private final ProjectQuotaEditPolicy editPolicy;
    private final ProjectQuotaMemberPolicy memberPolicy;
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
    }

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
            quotaAccessPolicy.requireManageChapter(command.requesterMemberId(), project.getGisuId(), project.getChapterId());
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
        Map<ChallengerPart, Long> minimums = minimumQuotas(project, rounds, now);
        Map<ChallengerPart, Long> requested = command.entries().stream()
            .collect(Collectors.toMap(Entry::part, Entry::quota));
        Set<ChallengerPart> changed = EnumSet.noneOf(ChallengerPart.class);
        changed.addAll(existingByPart.keySet());
        changed.addAll(requested.keySet());
        for (ChallengerPart part : changed) {
            long minimum = minimums.getOrDefault(part, 0L);
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
                    part.getDisplayName() + ": 배정·합격 인원보다 TO를 줄일 수 없어요.");
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

    /**
     * 파트별 최소 TO. 배정·발표 대기 인원(해제 예약자 제외)에 아직 자동 선발하지 않은 차수의 PM 합격 인원을 더한다.
     * 합격자는 자동 선발 때 팀원이 되므로 그 전에 TO를 줄이면 자리 없는 합격자가 생긴다. PM은 대표 PM을 포함한다.
     */
    Map<ChallengerPart, Long> minimumQuotas(Project project, List<ProjectMatchingRound> rounds, Instant now) {
        List<ProjectMember> members = loadProjectMemberPort.listActiveAndPendingPublicationByProjectId(project.getId());
        List<ProjectMember> reserved = memberPolicy.reservedMembers(members, rounds, now);
        Map<ChallengerPart, Long> minimums = reserved.stream()
            .filter(member -> member.getPart() != ChallengerPart.PLAN)
            .collect(Collectors.groupingBy(ProjectMember::getPart, () -> new EnumMap<>(ChallengerPart.class),
                Collectors.counting()));
        Set<Long> planMemberIds = new HashSet<>();
        planMemberIds.add(project.getProductOwnerMemberId());
        reserved.stream()
            .filter(member -> member.getPart() == ChallengerPart.PLAN)
            .map(ProjectMember::getMemberId).forEach(planMemberIds::add);
        minimums.put(ChallengerPart.PLAN, (long) planMemberIds.size());

        Set<Long> memberIds = members.stream().map(ProjectMember::getMemberId).collect(Collectors.toSet());
        List<ProjectApplication> approved = loadProjectApplicationPort.searchProjectApplications(
                project.getId(), null, ProjectApplicationStatus.APPROVED, now, true).stream()
            .filter(application -> !application.getAppliedMatchingRound().isAutoDecisionExecuted())
            .filter(application -> !memberIds.contains(application.getApplicantMemberId()))
            .toList();
        if (!approved.isEmpty()) {
            Map<Long, ChallengerInfo> challengers = getChallengerUseCase.batchGetByMemberIdsAndGisuId(
                approved.stream().map(ProjectApplication::getApplicantMemberId).collect(Collectors.toSet()),
                project.getGisuId());
            approved.stream()
                .map(application -> challengers.get(application.getApplicantMemberId()).part())
                .filter(Objects::nonNull)
                .forEach(part -> minimums.merge(part, 1L, Long::sum));
        }
        return minimums;
    }

    private void validateEntries(List<Entry> entries) {
        Set<ChallengerPart> seen = EnumSet.noneOf(ChallengerPart.class);
        for (Entry entry : entries) {
            if (entry.quota() < 1) {
                throw new ProjectDomainException(ProjectErrorCode.PROJECT_PART_QUOTA_INVALID);
            }
            if (entry.quota() > ProjectPartQuota.MAX_QUOTA) {
                throw new ProjectDomainException(ProjectErrorCode.PROJECT_PART_QUOTA_INVALID_REQUEST,
                    "TO는 " + ProjectPartQuota.MAX_QUOTA + "명 이하로 입력해주세요.");
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
