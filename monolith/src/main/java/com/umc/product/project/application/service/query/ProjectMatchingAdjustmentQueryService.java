package com.umc.product.project.application.service.query;

import java.time.Instant;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.authorization.application.port.in.CheckPermissionUseCase;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.dto.MemberInfo;
import com.umc.product.project.application.port.in.query.GetProjectMatchingAdjustmentUseCase;
import com.umc.product.project.application.port.in.query.dto.ProjectMatchingRosterInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectMatchingRosterInfo.RosterMemberInfo;
import com.umc.product.project.application.port.out.LoadProjectMatchingRoundPort;
import com.umc.product.project.application.port.out.LoadProjectMemberPort;
import com.umc.product.project.application.port.out.LoadProjectPort;
import com.umc.product.project.application.service.policy.ProjectManualMatchingAccessPolicy;
import com.umc.product.project.application.service.policy.ProjectMatchingAdjustmentPolicy;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectMatchingRound;
import com.umc.product.project.domain.ProjectMember;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;

import lombok.RequiredArgsConstructor;

/**
 * 운영진의 조정 권한을 검증하고 조정 명단을 조회한다. 조정 명단을 보여줄 시점이 아니면 현재 공개 명단을 반환한다.
 * 조회 도중 결과가 공개되더라도 차수의 공개 여부와 팀원 명단을 동일한 시점의 데이터로 조회한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
public class ProjectMatchingAdjustmentQueryService implements GetProjectMatchingAdjustmentUseCase {

    private final LoadProjectPort loadProjectPort;
    private final LoadProjectMatchingRoundPort loadProjectMatchingRoundPort;
    private final LoadProjectMemberPort loadProjectMemberPort;
    private final CheckPermissionUseCase checkPermissionUseCase;
    private final GetMemberUseCase getMemberUseCase;
    private final ProjectMatchingAdjustmentPolicy adjustmentPolicy;

    @Override
    public Map<Long, ProjectMatchingRosterInfo> get(List<Long> projectIds, Long requesterMemberId) {
        if (projectIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, Project> projects = loadProjectPort.listByIds(projectIds).stream()
            .collect(Collectors.toMap(Project::getId, Function.identity()));
        var authority = checkPermissionUseCase.loadSubject(requesterMemberId).toAuthoritySnapshot();
        // 공개 명단을 반환하는 경우에도 조정 권한을 확인한다. 하나라도 권한이 없으면 요청 전체를 거부한다.
        for (Project project : projects.values()) {
            if (!ProjectManualMatchingAccessPolicy.canManage(authority, project)) {
                throw new ProjectDomainException(ProjectErrorCode.PROJECT_ACCESS_DENIED);
            }
        }
        if (!projects.keySet().containsAll(projectIds)) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_NOT_FOUND);
        }

        Instant now = Instant.now();
        Set<Long> adjustmentRoundIds = projects.values().stream()
            .map(Project::getChapterId)
            .distinct()
            .map(chapterId -> adjustmentPolicy.findViewableAdjustmentRounds(
                loadProjectMatchingRoundPort.listByChapterId(chapterId), now))
            .flatMap(List::stream)
            .map(ProjectMatchingRound::getId)
            .collect(Collectors.toSet());
        List<ProjectMember> members = loadProjectMemberPort
            .listAdjustmentMembersByProjectIds(projects.keySet(), adjustmentRoundIds);
        Set<Long> memberIds = new HashSet<>();
        projects.values().forEach(project -> memberIds.add(project.getProductOwnerMemberId()));
        members.forEach(member -> memberIds.add(member.getMemberId()));
        Map<Long, MemberInfo> people = getMemberUseCase.findAllByIds(memberIds);
        Map<Long, List<ProjectMember>> membersByProject = members.stream()
            .collect(Collectors.groupingBy(member -> member.getProject().getId()));

        Map<Long, ProjectMatchingRosterInfo> result = new LinkedHashMap<>();
        for (Long projectId : projectIds) {
            Project project = projects.get(projectId);
            List<RosterMemberInfo> roster = membersByProject.getOrDefault(projectId, List.of()).stream()
                .filter(member -> people.containsKey(member.getMemberId()))
                .map(member -> toInfo(people.get(member.getMemberId()), member.getPart(),
                    member.getAssignedMatchingRound()))
                .toList();
            RosterMemberInfo owner = roster.stream()
                .filter(member -> member.memberId().equals(project.getProductOwnerMemberId())).findFirst()
                .orElseGet(() -> toInfo(people.get(project.getProductOwnerMemberId()), ChallengerPart.PLAN, null));
            result.put(projectId, new ProjectMatchingRosterInfo(projectId, owner, roster));
        }
        return result;
    }

    private RosterMemberInfo toInfo(MemberInfo member, ChallengerPart part, ProjectMatchingRound round) {
        return member == null ? null : new RosterMemberInfo(
            member.id(), member.nickname(), member.name(), member.schoolName(),
            part, round == null ? null : round.getId(), round == null ? null : round.getType(),
            round == null ? null : round.getPhase());
    }
}
