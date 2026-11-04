package com.umc.product.project.application.service.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.umc.product.project.application.port.out.LoadProjectMemberPort;
import com.umc.product.project.application.port.out.LoadProjectPartQuotaPort;
import com.umc.product.project.application.port.out.LoadProjectPort;
import com.umc.product.project.application.port.out.SaveProjectPartQuotaPort;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectMatchingRound;
import com.umc.product.project.domain.ProjectPartQuota;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.enums.ProjectStatus;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
class ProjectMatchingQuotaPublicationService {
    private final LoadProjectPort loadProjectPort;
    private final LoadProjectMemberPort loadProjectMemberPort;
    private final LoadProjectPartQuotaPort loadProjectPartQuotaPort;
    private final SaveProjectPartQuotaPort saveProjectPartQuotaPort;

    /** 팀원 공개와 같은 트랜잭션에서 최종 활동 인원까지 TO를 늘린다. 기존 TO를 줄이지 않는다. */
    void synchronize(ProjectMatchingRound round) {
        List<Project> projects = loadProjectPort.listByChapterIdAndStatus(round.getChapterId(), ProjectStatus.IN_PROGRESS);
        List<Long> projectIds = projects.stream().map(Project::getId).toList();
        if (projectIds.isEmpty()) {
            return;
        }
        var counts = loadProjectMemberPort.countByProjectIdsGroupByProjectIdAndPart(projectIds);
        var quotas = loadProjectPartQuotaPort.listByProjectIdsGroupedByProjectId(projectIds);
        List<ProjectPartQuota> changed = new ArrayList<>();
        for (Project project : projects) {
            var existing = quotas.getOrDefault(project.getId(), List.of());
            counts.getOrDefault(project.getId(), Map.of()).forEach((part, count) -> {
                if (count <= 0 || MatchingType.fromPart(part).orElse(null) != round.getType()) {
                    return;
                }
                var quota = existing.stream().filter(value -> value.getPart() == part).findFirst().orElse(null);
                if (quota == null) {
                    changed.add(ProjectPartQuota.create(project, part, count, round.getAutoDecisionExecutedMemberId()));
                } else if (count > quota.getQuota()) {
                    quota.updateQuota(count, round.getAutoDecisionExecutedMemberId());
                    changed.add(quota);
                }
            });
        }
        if (!changed.isEmpty()) {
            saveProjectPartQuotaPort.saveAll(changed);
        }
    }
}
