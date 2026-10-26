package com.umc.product.project.application.service.query;

import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.authorization.application.port.in.CheckPermissionUseCase;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.dto.MemberInfo;
import com.umc.product.project.application.port.in.query.GetProjectPublishedMembersUseCase;
import com.umc.product.project.application.port.in.query.dto.ProjectMatchingRosterInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectMatchingRosterInfo.RosterMemberInfo;
import com.umc.product.project.application.port.out.LoadProjectMemberPort;
import com.umc.product.project.application.port.out.LoadProjectPort;
import com.umc.product.project.application.port.out.dto.ProjectMemberMatchedRoundInfo;
import com.umc.product.project.application.service.evaluator.ProjectPermissionEvaluator;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectMember;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 공개된 프로젝트 팀원과 선발 차수를 일괄 조회한다.
 * 조회 도중 결과가 공개되어도 명단과 선발 차수는 동일한 시점의 데이터로 조회한다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
public class ProjectPublishedMembersQueryService implements GetProjectPublishedMembersUseCase {

    private final CheckPermissionUseCase checkPermissionUseCase;
    private final LoadProjectPort loadProjectPort;
    private final ProjectPermissionEvaluator projectPermissionEvaluator;
    private final LoadProjectMemberPort loadProjectMemberPort;
    private final GetMemberUseCase getMemberUseCase;

    @Override
    public Map<Long, ProjectMatchingRosterInfo> get(List<Long> projectIds, Long memberId) {
        if (projectIds.isEmpty()) {
            return Map.of();
        }
        // Step 1: 프로젝트를 일괄 조회한 뒤, 권한이 없거나 존재하지 않는 프로젝트는 제외한다.
        Map<Long, Project> projectsById = loadProjectPort.listByIds(projectIds).stream()
            .collect(Collectors.toMap(Project::getId, project -> project));
        var subject = checkPermissionUseCase.loadSubject(memberId);
        Map<Long, Project> validProjects = new LinkedHashMap<>();
        for (Long projectId : projectIds) {
            Project project = projectsById.get(projectId);
            if (project == null) {
                continue;
            }
            if (!projectPermissionEvaluator.canRead(subject, project)) {
                log.warn("프로젝트 팀원 일괄 조회 - 접근 권한 없음: memberId={}, projectId={}", memberId, projectId);
                continue;
            }
            validProjects.put(projectId, project);
        }

        if (validProjects.isEmpty()) {
            return Map.of();
        }

        // Step 2: 유효한 전체 프로젝트의 파트 멤버를 IN 쿼리 한 번으로 조회 후 메모리 grouping
        Map<Long, List<ProjectMember>> projectMembersMap =
            loadProjectMemberPort.listByProjectIds(validProjects.keySet());

        // Step 3: 유효한 전체 프로젝트의 멤버 ID를 모아 한 번에 조회
        Set<Long> allMemberIds = new HashSet<>();
        validProjects.forEach((projectId, info) -> {
            allMemberIds.add(info.getProductOwnerMemberId());
            projectMembersMap.getOrDefault(projectId, List.of())
                .forEach(m -> allMemberIds.add(m.getMemberId()));
        });
        Map<Long, MemberInfo> memberMap = allMemberIds.isEmpty()
            ? Map.of()
            : getMemberUseCase.findAllByIds(allMemberIds);
        Map<ProjectMemberKey, ProjectMemberMatchedRoundInfo> matchedRoundMap = loadProjectMemberPort
            .listMatchedRoundsByProjectIdsAndMemberIds(validProjects.keySet(), allMemberIds).stream()
            .collect(Collectors.toMap(row -> new ProjectMemberKey(row.projectId(), row.memberId()),
                row -> row, (left, right) -> left));

        Map<Long, ProjectMatchingRosterInfo> result = new LinkedHashMap<>();
        validProjects.forEach((projectId, info) -> {
            List<RosterMemberInfo> members = projectMembersMap.getOrDefault(projectId, List.of()).stream()
                .sorted(Comparator.comparing(ProjectMember::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(ProjectMember::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(member -> toInfo(memberMap.get(member.getMemberId()), member.getPart(),
                    matchedRoundMap.get(new ProjectMemberKey(projectId, member.getMemberId()))))
                .filter(Objects::nonNull)
                .toList();
            RosterMemberInfo owner = toInfo(memberMap.get(info.getProductOwnerMemberId()), ChallengerPart.PLAN,
                matchedRoundMap.get(new ProjectMemberKey(projectId, info.getProductOwnerMemberId())));
            result.put(projectId, new ProjectMatchingRosterInfo(projectId, owner, members));
        });
        return result;
    }

    private RosterMemberInfo toInfo(MemberInfo member, ChallengerPart part, ProjectMemberMatchedRoundInfo round) {
        return member == null ? null : new RosterMemberInfo(member.id(), member.nickname(), member.name(), member.schoolName(),
            part, round == null ? null : round.matchingRoundId(), round == null ? null : round.matchingRoundType(),
            round == null ? null : round.matchingRoundPhase());
    }

    private record ProjectMemberKey(Long projectId, Long memberId) {
    }
}
