package com.umc.product.project.application.service.query;

import com.umc.product.project.application.port.in.query.GetProjectStatisticsUseCase;
import com.umc.product.project.application.port.in.query.dto.statistics.ProjectMatchingRoundStatisticsInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.ProjectMemberApplicationStatisticsInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.ProjectMemberStatisticsInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.ProjectStatisticsInfo;
import com.umc.product.project.application.port.out.LoadProjectStatisticsPort;
import com.umc.product.project.application.port.out.dto.ProjectStatisticsApplicationRow;
import com.umc.product.project.application.port.out.dto.ProjectStatisticsMemberRow;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectStatisticsQueryService implements GetProjectStatisticsUseCase {

    private final LoadProjectStatisticsPort loadProjectStatisticsPort;

    @Override
    public ProjectStatisticsInfo getByProjectId(Long projectId) {
        List<ProjectStatisticsInfo> statistics = assemble(
            loadProjectStatisticsPort.listActiveMembersByProjectId(projectId));

        return statistics.stream()
            .findFirst()
            .orElseGet(() -> new ProjectStatisticsInfo(projectId, List.of()));
    }

    @Override
    public List<ProjectStatisticsInfo> listByChapterId(Long chapterId) {
        return assemble(loadProjectStatisticsPort.listActiveMembersByChapterId(chapterId));
    }

    private List<ProjectStatisticsInfo> assemble(List<ProjectStatisticsMemberRow> memberRows) {
        if (memberRows.isEmpty()) {
            return List.of();
        }

        List<ProjectStatisticsMemberRow> sortedMembers = memberRows.stream()
            .sorted(Comparator
                .comparing(ProjectStatisticsMemberRow::projectId)
                .thenComparing(ProjectStatisticsMemberRow::projectMemberId))
            .toList();

        Set<Long> projectIds = sortedMembers.stream()
            .map(ProjectStatisticsMemberRow::projectId)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<Long> memberIds = sortedMembers.stream()
            .map(ProjectStatisticsMemberRow::memberId)
            .collect(Collectors.toCollection(LinkedHashSet::new));

        Map<ProjectMemberKey, List<ProjectStatisticsApplicationRow>> applicationsByMember =
            groupApplications(loadProjectStatisticsPort.listCountedApplicationsByProjectIdsAndMemberIds(
                projectIds, memberIds));

        Map<Long, List<ProjectStatisticsMemberRow>> membersByProject = sortedMembers.stream()
            .collect(Collectors.groupingBy(
                ProjectStatisticsMemberRow::projectId,
                LinkedHashMap::new,
                Collectors.toList()
            ));

        return membersByProject.entrySet().stream()
            .map(entry -> new ProjectStatisticsInfo(
                entry.getKey(),
                entry.getValue().stream()
                    .map(member -> toMemberInfo(member, applicationsByMember.getOrDefault(
                        ProjectMemberKey.from(member), List.of())))
                    .toList()
            ))
            .toList();
    }

    private Map<ProjectMemberKey, List<ProjectStatisticsApplicationRow>> groupApplications(
        Collection<ProjectStatisticsApplicationRow> applications
    ) {
        return applications.stream()
            .sorted(Comparator
                .comparing(ProjectStatisticsApplicationRow::projectId)
                .thenComparing(ProjectStatisticsApplicationRow::applicantMemberId)
                .thenComparing(ProjectStatisticsApplicationRow::matchingRoundType)
                .thenComparing(ProjectStatisticsApplicationRow::matchingRoundPhase)
                .thenComparing(ProjectStatisticsApplicationRow::applicationId))
            .collect(Collectors.groupingBy(
                ProjectMemberKey::from,
                LinkedHashMap::new,
                Collectors.toList()
            ));
    }

    private ProjectMemberStatisticsInfo toMemberInfo(
        ProjectStatisticsMemberRow member,
        List<ProjectStatisticsApplicationRow> applications
    ) {
        return new ProjectMemberStatisticsInfo(
            member.projectMemberId(),
            member.memberId(),
            member.part(),
            member.status(),
            applications.stream()
                .map(this::toApplicationInfo)
                .toList()
        );
    }

    private ProjectMemberApplicationStatisticsInfo toApplicationInfo(ProjectStatisticsApplicationRow row) {
        return new ProjectMemberApplicationStatisticsInfo(
            row.applicationId(),
            row.status(),
            new ProjectMatchingRoundStatisticsInfo(
                row.matchingRoundId(),
                row.matchingRoundType(),
                row.matchingRoundPhase()
            )
        );
    }

    private record ProjectMemberKey(Long projectId, Long memberId) {

        private static ProjectMemberKey from(ProjectStatisticsMemberRow row) {
            return new ProjectMemberKey(row.projectId(), row.memberId());
        }

        private static ProjectMemberKey from(ProjectStatisticsApplicationRow row) {
            return new ProjectMemberKey(row.projectId(), row.applicantMemberId());
        }
    }
}
