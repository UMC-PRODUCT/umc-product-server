package com.umc.product.project.application.service.command;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.audit.application.port.in.annotation.Audited;
import com.umc.product.audit.domain.AuditAction;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.project.application.access.ProjectQuotaAccessPolicy;
import com.umc.product.project.application.port.in.command.SaveProjectQuotaManagementUseCase;
import com.umc.product.project.application.port.in.command.dto.SaveProjectQuotaManagementCommand;
import com.umc.product.project.application.port.in.command.dto.UpdatePartQuotasCommand;
import com.umc.product.project.application.port.out.LoadProjectMemberPort;
import com.umc.product.project.application.port.out.LoadProjectPartQuotaPort;
import com.umc.product.project.application.port.out.LoadProjectPort;
import com.umc.product.project.application.port.out.LockProjectMatchingPort;
import com.umc.product.project.application.service.policy.ProjectQuotaCapacityPolicy;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectQuotaManagementCommandService implements SaveProjectQuotaManagementUseCase {
    private final ProjectQuotaAccessPolicy accessPolicy;
    private final LockProjectMatchingPort lockProjectMatchingPort;
    private final LoadProjectPort loadProjectPort;
    private final LoadProjectMemberPort loadProjectMemberPort;
    private final LoadProjectPartQuotaPort loadProjectPartQuotaPort;
    private final ProjectPartQuotaCommandService updateService;
    private final ProjectQuotaCapacityPolicy capacityPolicy;

    @Override
    @Audited(domain = Domain.PROJECT, action = AuditAction.UPDATE, targetType = "ProjectQuotaManagement",
        targetId = "#command.chapterId()", description = "'프로젝트 TO를 일괄 저장했습니다.'")
    public List<Long> save(SaveProjectQuotaManagementCommand command) {
        accessPolicy.requireChapter(command.requesterId(), command.gisuId(), command.chapterId());
        validate(command);
        lockProjectMatchingPort.lockByGisuId(command.gisuId());
        var assigned = loadProjectMemberPort.listAssignedByProjectIds(command.projects().stream()
            .map(SaveProjectQuotaManagementCommand.ProjectChange::projectId).toList());
        for (var change : command.projects().stream()
            .sorted(Comparator.comparing(SaveProjectQuotaManagementCommand.ProjectChange::projectId)).toList()) {
            var project = loadProjectPort.getById(change.projectId());
            if (!Objects.equals(project.getGisuId(), command.gisuId())
                || !Objects.equals(project.getChapterId(), command.chapterId())) {
                throw new ProjectDomainException(ProjectErrorCode.PROJECT_ACCESS_DENIED);
            }
            var quotas = new EnumMap<ChallengerPart, Long>(ChallengerPart.class);
            loadProjectPartQuotaPort.listByProjectId(change.projectId())
                .forEach(quota -> quotas.put(quota.getPart(), quota.getQuota()));
            for (var entry : change.entries()) {
                var memberIds = new HashSet<Long>();
                if (entry.part() == ChallengerPart.PLAN) {
                    memberIds.add(project.getProductOwnerMemberId());
                }
                assigned.getOrDefault(project.getId(), List.of()).stream()
                    .filter(member -> member.getPart() == entry.part())
                    .map(com.umc.product.project.domain.ProjectMember::getMemberId).forEach(memberIds::add);
                if (entry.quota() < memberIds.size()) {
                    throw new ProjectDomainException(ProjectErrorCode.PROJECT_PART_QUOTA_BELOW_ASSIGNED,
                        project.getId() + " / " + entry.part().getDisplayName() + ": 이미 배정된 인원보다 TO가 적어요.");
                }
                if (entry.quota() == 0) {
                    quotas.remove(entry.part());
                } else {
                    quotas.put(entry.part(), entry.quota());
                }
            }
            updateService.updateWithinBatch(UpdatePartQuotasCommand.builder().projectId(change.projectId())
                .requesterMemberId(command.requesterId())
                .entries(quotas.entrySet().stream()
                    .map(entry -> new UpdatePartQuotasCommand.Entry(entry.getKey(), entry.getValue())).toList()).build());
        }
        // 프로젝트 간 TO 이동은 중간 상태가 아니라 모든 변경을 합친 최종 상태로 검증한다.
        capacityPolicy.validate(command.gisuId(), command.chapterId());
        return command.projects().stream().map(SaveProjectQuotaManagementCommand.ProjectChange::projectId).toList();
    }

    private void validate(SaveProjectQuotaManagementCommand command) {
        var projects = new HashSet<Long>();
        if (command.projects().isEmpty() || command.projects().size() > 500) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_PART_QUOTA_INVALID_REQUEST);
        }
        for (var project : command.projects()) {
            var parts = new HashSet<ChallengerPart>();
            if (!projects.add(project.projectId()) || project.entries().isEmpty() || project.entries().size() > 10) {
                throw new ProjectDomainException(ProjectErrorCode.PROJECT_PART_QUOTA_INVALID_REQUEST);
            }
            for (var entry : project.entries()) {
                if (entry.part() == ChallengerPart.ADMIN) {
                    throw new ProjectDomainException(ProjectErrorCode.PROJECT_PART_QUOTA_READ_ONLY);
                }
                if (!parts.add(entry.part()) || entry.quota() < 0) {
                    throw new ProjectDomainException(ProjectErrorCode.PROJECT_PART_QUOTA_INVALID_REQUEST);
                }
            }
        }
    }
}
