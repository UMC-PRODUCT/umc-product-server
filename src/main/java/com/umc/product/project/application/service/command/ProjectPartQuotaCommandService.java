package com.umc.product.project.application.service.command;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.application.port.in.command.UpdatePartQuotasUseCase;
import com.umc.product.project.application.port.in.command.dto.UpdatePartQuotasCommand;
import com.umc.product.project.application.port.in.command.dto.UpdatePartQuotasCommand.Entry;
import com.umc.product.project.application.port.out.LoadProjectPartQuotaPort;
import com.umc.product.project.application.port.out.LoadProjectPort;
import com.umc.product.project.application.port.out.SaveProjectPartQuotaPort;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectPartQuota;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectPartQuotaCommandService implements UpdatePartQuotasUseCase {

    private final LoadProjectPort loadProjectPort;
    private final LoadProjectPartQuotaPort loadProjectPartQuotaPort;
    private final SaveProjectPartQuotaPort saveProjectPartQuotaPort;

    /**
     * PUT 시멘틱 — 본문에 있는 파트만 유지/갱신, 본문에 없는 기존 파트는 삭제.
     */
    @Override
    public void update(UpdatePartQuotasCommand command) {
        Project project = loadProjectPort.getById(command.projectId());
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
    }
}
