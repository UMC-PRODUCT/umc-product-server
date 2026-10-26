package com.umc.product.project.application.service.query;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerStatus;
import com.umc.product.project.application.port.in.query.dto.SearchProjectQuery;
import com.umc.product.project.application.port.out.LoadProjectMemberPort;
import com.umc.product.project.application.port.out.LoadProjectPartQuotaPort;
import com.umc.product.project.application.port.out.LoadProjectPort;
import com.umc.product.project.application.service.policy.ProjectMatchingChapterResolver;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectMember;
import com.umc.product.project.domain.ProjectPartQuota;
import com.umc.product.project.domain.enums.ProjectMemberStatus;
import com.umc.product.project.domain.enums.ProjectStatus;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProjectQuotaManagementDataLoader {
    private final LoadProjectPort loadProjectPort;
    private final LoadProjectPartQuotaPort loadProjectPartQuotaPort;
    private final LoadProjectMemberPort loadProjectMemberPort;
    private final GetChallengerUseCase getChallengerUseCase;
    private final ProjectMatchingChapterResolver matchingChapterResolver;

    public List<Project> projects(Long gisuId, Long chapterId, String keyword, Long schoolId) {
        return loadProjectPort.search(SearchProjectQuery.forAdmin(gisuId, keyword, chapterId,
            schoolId == null ? null : List.of(schoolId), null, null,
            List.of(ProjectStatus.PENDING_REVIEW, ProjectStatus.IN_PROGRESS),
            Pageable.unpaged(Sort.by("name", "id")))).getContent();
    }

    public Snapshot load(Long gisuId, Long chapterId) {
        List<Project> projects = projects(gisuId, chapterId, null, null);
        var ids = projects.stream().map(Project::getId).toList();
        Map<ChallengerPart, Long> population = new EnumMap<>(ChallengerPart.class);
        matchingChapterResolver.deltaOf(chapterId).apply(getChallengerUseCase.listByChapterId(chapterId)).stream()
            .filter(challenger -> challenger.challengerStatus() == ChallengerStatus.ACTIVE)
            .forEach(challenger -> population.merge(challenger.part(), 1L, Math::addExact));
        return new Snapshot(projects, loadProjectPartQuotaPort.listByProjectIdsGroupedByProjectId(ids),
            loadProjectMemberPort.listAssignedByProjectIds(ids), population);
    }

    public record Snapshot(
        List<Project> projects, Map<Long, List<ProjectPartQuota>> quotas,
        Map<Long, List<ProjectMember>> assignedMembers, Map<ChallengerPart, Long> population
    ) {
        public long quota(Project project, ChallengerPart part) {
            return quotas.getOrDefault(project.getId(), List.of()).stream().filter(quota -> quota.getPart() == part)
                .mapToLong(ProjectPartQuota::getQuota).findFirst()
                .orElse(part == ChallengerPart.PLAN ? minimum(project, part) : 0);
        }

        public long current(Project project, ChallengerPart part) {
            return count(project, part, false);
        }

        public long minimum(Project project, ChallengerPart part) {
            return count(project, part, true);
        }

        private long count(Project project, ChallengerPart part, boolean includePending) {
            var ids = new HashSet<Long>();
            if (part == ChallengerPart.PLAN) {
                ids.add(project.getProductOwnerMemberId());
            }
            assignedMembers.getOrDefault(project.getId(), List.of()).stream()
                .filter(member -> member.getPart() == part)
                .filter(member -> includePending || member.getStatus() == ProjectMemberStatus.ACTIVE)
                .map(ProjectMember::getMemberId).forEach(ids::add);
            return ids.size();
        }

        public Map<ChallengerPart, Long> allocated(Collection<Project> targets) {
            Map<ChallengerPart, Long> result = new EnumMap<>(ChallengerPart.class);
            for (Project project : targets) {
                result.merge(ChallengerPart.PLAN, quota(project, ChallengerPart.PLAN), Math::addExact);
                for (ProjectPartQuota quota : quotas.getOrDefault(project.getId(), List.of())) {
                    if (quota.getPart() != ChallengerPart.PLAN && quota.getPart() != ChallengerPart.ADMIN) {
                        result.merge(quota.getPart(), quota.getQuota(), Math::addExact);
                    }
                }
            }
            return result;
        }
    }
}
