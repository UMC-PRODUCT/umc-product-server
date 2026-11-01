package com.umc.product.project.application.service.query;

import java.time.Clock;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.dto.MemberInfo;
import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import com.umc.product.project.application.port.in.query.GetProjectMatchingOverviewUseCase;
import com.umc.product.project.application.port.in.query.dto.ProjectMatchingOverviewInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectMatchingOverviewInfo.MatchingOverviewMemberInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectMatchingOverviewInfo.MatchingOverviewOwnerInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectMatchingOverviewInfo.MatchingOverviewPartInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectMatchingOverviewInfo.MatchingOverviewProjectInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectMatchingOverviewInfo.MatchingOverviewRoundInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectMemberView;
import com.umc.product.project.application.port.out.LoadProjectMatchingRoundPort;
import com.umc.product.project.application.port.out.LoadProjectMemberPort;
import com.umc.product.project.application.port.out.LoadProjectPartQuotaPort;
import com.umc.product.project.application.port.out.LoadProjectPort;
import com.umc.product.project.application.service.policy.ProjectManualMatchingAccessPolicy;
import com.umc.product.project.application.service.policy.ProjectMatchingAdjustmentPolicy;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectMatchingRound;
import com.umc.product.project.domain.ProjectMember;
import com.umc.product.project.domain.ProjectPartQuota;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.enums.PartQuotaStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectMatchingOverviewQueryService implements GetProjectMatchingOverviewUseCase {

    private final LoadProjectPort loadProjectPort;
    private final LoadProjectMemberPort loadProjectMemberPort;
    private final LoadProjectPartQuotaPort loadProjectPartQuotaPort;
    private final LoadProjectMatchingRoundPort loadProjectMatchingRoundPort;
    private final GetChapterUseCase getChapterUseCase;
    private final GetMemberUseCase getMemberUseCase;
    private final ProjectManualMatchingAccessPolicy manualMatchingAccessPolicy;
    private final ProjectMatchingAdjustmentPolicy matchingAdjustmentPolicy;
    private final Clock clock;

    @Override
    public ProjectMatchingOverviewInfo getByChapterId(
        Long chapterId,
        ProjectMemberView view,
        Long requesterMemberId
    ) {
        Long gisuId = getChapterUseCase.getGisuIdByChapterId(chapterId);
        if (view == ProjectMemberView.ADJUSTMENT) {
            manualMatchingAccessPolicy.validateChapter(requesterMemberId, gisuId, chapterId);
        }
        List<Project> targets = loadProjectPort.listPublicByChapterId(chapterId);
        if (targets.isEmpty()) {
            return ProjectMatchingOverviewInfo.empty();
        }
        Set<Long> projectIds = targets.stream()
            .map(Project::getId)
            .collect(Collectors.toSet());
        List<Long> adjustmentRoundIds = resolveAdjustmentRoundIds(chapterId, view);
        List<ProjectMember> members = loadProjectMemberPort
            .listMatchingOverviewMembersByProjectIds(projectIds, adjustmentRoundIds);
        Map<Long, List<ProjectMember>> membersByProject = members.stream()
            .collect(Collectors.groupingBy(member -> member.getProject().getId()));
        Map<Long, List<ProjectPartQuota>> quotasByProject =
            loadProjectPartQuotaPort.listByProjectIdsGroupedByProjectId(projectIds);
        Set<Long> memberIds = members.stream()
            .map(ProjectMember::getMemberId)
            .collect(Collectors.toSet());
        targets.forEach(project -> memberIds.add(project.getProductOwnerMemberId()));
        Map<Long, MemberInfo> memberInfoById = memberIds.isEmpty() ? Map.of() : getMemberUseCase.findAllByIds(memberIds);

        List<MatchingOverviewProjectInfo> projects = targets.stream()
            .map(target -> toMatchingOverviewProject(
                target,
                membersByProject.getOrDefault(target.getId(), List.of()),
                quotasByProject.getOrDefault(target.getId(), List.of()),
                memberInfoById))
            .toList();
        return new ProjectMatchingOverviewInfo(projects);
    }

    private List<Long> resolveAdjustmentRoundIds(Long chapterId, ProjectMemberView view) {
        if (view != ProjectMemberView.ADJUSTMENT) {
            return List.of();
        }
        List<ProjectMatchingRound> rounds = loadProjectMatchingRoundPort.listByChapterId(chapterId);
        return matchingAdjustmentPolicy.findViewableAdjustmentRounds(rounds, clock.instant()).stream()
            .map(ProjectMatchingRound::getId)
            .toList();
    }

    private MatchingOverviewProjectInfo toMatchingOverviewProject(
        Project project,
        List<ProjectMember> members,
        List<ProjectPartQuota> quotas,
        Map<Long, MemberInfo> memberInfoById
    ) {
        Long productOwnerMemberId = project.getProductOwnerMemberId();
        List<MatchingOverviewOwnerInfo> coProductOwners = members.stream()
            .filter(member -> member.getPart() == ChallengerPart.PLAN)
            .filter(member -> !member.getMemberId().equals(productOwnerMemberId))
            .map(ProjectMember::getMemberId)
            .map(memberId -> toMatchingOverviewOwnerInfo(memberId, memberInfoById.get(memberId)))
            .toList();
        List<MatchingOverviewPartInfo> partInfos = buildParts(members, quotas, memberInfoById);
        PartQuotaStatus status;
        if (quotas.stream().noneMatch(quota -> MatchingType.fromPart(quota.getPart()).isPresent())) {
            status = null;
        } else {
            boolean recruiting = partInfos.stream().anyMatch(part -> part.currentMemberCount() < part.quota());
            status = recruiting ? PartQuotaStatus.RECRUITING : PartQuotaStatus.COMPLETED;
        }
        return new MatchingOverviewProjectInfo(
            project.getId(),
            project.getName(),
            partInfos.stream().mapToLong(MatchingOverviewPartInfo::currentMemberCount).sum(),
            partInfos.stream().mapToLong(MatchingOverviewPartInfo::quota).sum(),
            status,
            toMatchingOverviewOwnerInfo(productOwnerMemberId, memberInfoById.get(productOwnerMemberId)),
            coProductOwners,
            partInfos
        );
    }

    private List<MatchingOverviewPartInfo> buildParts(
        List<ProjectMember> members,
        List<ProjectPartQuota> quotas,
        Map<Long, MemberInfo> memberInfoById
    ) {
        Map<ChallengerPart, List<ProjectMember>> membersByPart = members.stream()
            .filter(member -> MatchingType.fromPart(member.getPart()).isPresent())
            .collect(Collectors.groupingBy(ProjectMember::getPart));
        Map<ChallengerPart, Long> quotasByPart = quotas.stream()
            .filter(quota -> MatchingType.fromPart(quota.getPart()).isPresent())
            .collect(Collectors.toMap(ProjectPartQuota::getPart, ProjectPartQuota::getQuota));
        Set<ChallengerPart> parts = new HashSet<>(quotasByPart.keySet());
        parts.addAll(membersByPart.keySet());
        return parts.stream()
            .sorted()
            .map(part -> toMatchingOverviewPart(
                part,
                membersByPart.getOrDefault(part, List.of()),
                quotasByPart.getOrDefault(part, 0L),
                memberInfoById))
            .toList();
    }

    private MatchingOverviewPartInfo toMatchingOverviewPart(
        ChallengerPart part,
        List<ProjectMember> members,
        long quota,
        Map<Long, MemberInfo> memberInfoById
    ) {
        List<MatchingOverviewMemberInfo> memberInfos = members.stream()
            .map(member -> toMatchingOverviewMemberInfo(member, memberInfoById.get(member.getMemberId())))
            .toList();
        PartQuotaStatus status = memberInfos.size() < quota
            ? PartQuotaStatus.RECRUITING
            : PartQuotaStatus.COMPLETED;
        return new MatchingOverviewPartInfo(part, memberInfos.size(), quota, status, memberInfos);
    }

    private MatchingOverviewOwnerInfo toMatchingOverviewOwnerInfo(Long memberId, MemberInfo memberInfo) {
        return new MatchingOverviewOwnerInfo(
            memberId,
            memberInfo == null ? null : memberInfo.nickname(),
            memberInfo == null ? null : memberInfo.name(),
            memberInfo == null ? null : memberInfo.schoolName()
        );
    }

    private MatchingOverviewMemberInfo toMatchingOverviewMemberInfo(ProjectMember member, MemberInfo memberInfo) {
        ProjectMatchingRound round = member.getAssignedMatchingRound();
        return new MatchingOverviewMemberInfo(
            member.getMemberId(),
            memberInfo == null ? null : memberInfo.nickname(),
            memberInfo == null ? null : memberInfo.name(),
            memberInfo == null ? null : memberInfo.schoolName(),
            round == null ? null : new MatchingOverviewRoundInfo(round.getId(), round.getType(), round.getPhase())
        );
    }

}
