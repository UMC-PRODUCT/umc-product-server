package com.umc.product.project.application.service.command;

import com.umc.product.authorization.application.port.in.query.GetChallengerRoleUseCase;
import com.umc.product.authorization.application.port.in.query.dto.ChallengerRoleInfo;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.member.application.port.in.query.GetMemberRoleUseCase;
import com.umc.product.project.application.port.in.command.AutoDecideProjectMatchingRoundUseCase;
import com.umc.product.project.application.port.out.LoadProjectApplicationPort;
import com.umc.product.project.application.port.out.LoadProjectMatchingRoundPort;
import com.umc.product.project.application.port.out.LoadProjectMemberPort;
import com.umc.product.project.application.port.out.LoadProjectPartQuotaPort;
import com.umc.product.project.application.port.out.SaveProjectApplicationPort;
import com.umc.product.project.application.port.out.SaveProjectMemberPort;
import com.umc.product.project.application.service.policy.AutoDecisionResult;
import com.umc.product.project.application.service.policy.MatchingDecisionPolicy;
import com.umc.product.project.domain.ProjectApplication;
import com.umc.product.project.domain.ProjectMatchingRound;
import com.umc.product.project.domain.ProjectMember;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.enums.ProjectApplicationStatus;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 매칭 차수 종료 시점에 자동 선발을 실행하는 서비스 (MATCHING-201 + 스케줄러 공통 진입점).
 * <p>
 * CRUD lifecycle (생성/수정/삭제) 은 {@link ProjectMatchingRoundCommandService} 가 책임지며,
 * 본 서비스는 lifecycle 종료 트리거({@code decisionDeadline} 도달) 후의 처리만 담당한다.
 * <p>
 * CRUD 와 분리한 이유: lifecycle 호출 흐름({@code Service → Registry})과 트리거 흐름
 * ({@code Registry → Service}) 의 양 방향이 한 클래스에 모이면 순환 의존성이 발생하기 때문이다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ProjectMatchingRoundFinalizationCommandService implements
    AutoDecideProjectMatchingRoundUseCase {

    private final LoadProjectMatchingRoundPort loadProjectMatchingRoundPort;
    private final LoadProjectApplicationPort loadProjectApplicationPort;
    private final SaveProjectApplicationPort saveProjectApplicationPort;
    private final LoadProjectPartQuotaPort loadProjectPartQuotaPort;
    private final LoadProjectMemberPort loadProjectMemberPort;
    private final SaveProjectMemberPort saveProjectMemberPort;
    private final List<MatchingDecisionPolicy> matchingDecisionPolicies;
    private final Random matchingRandom;
    private final GetChallengerRoleUseCase getChallengerRoleUseCase;
    private final GetMemberRoleUseCase getMemberRoleUseCase;
    private final GetChallengerUseCase getChallengerUseCase;

    @Override
    public void autoDecide(Long matchingRoundId, Long executedByMemberId) {
        ProjectMatchingRound round = loadProjectMatchingRoundPort.getById(matchingRoundId);

        // executedByMemberId == null 은 스케줄러 호출. 권한 검증은 운영진 수동 호출에만 적용.
        if (executedByMemberId != null) {
            validateManageAccess(executedByMemberId, round.getChapterId());
        }

        if (round.getAutoDecisionExecutedAt() != null) {
            return;
        }
        if (!round.isDecisionDeadlinePassed(Instant.now())) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_MATCHING_ROUND_NOT_FINALIZABLE);
        }

        MatchingDecisionPolicy policy = resolvePolicy(round.getType());

        List<ProjectApplication> applicants = loadProjectApplicationPort
            .listByMatchingRoundId(matchingRoundId).stream()
            .filter(a -> isDecidableStatus(a.getStatus()))
            .toList();

        if (applicants.isEmpty()) {
            round.executeAutoDecision(executedByMemberId);
            return;
        }

        Map<Long, ChallengerPart> applicantPart = resolveApplicantParts(applicants);
        Map<ProjectPartKey, List<ProjectApplication>> grouped = groupByProjectAndPart(applicants, applicantPart);

        Map<ProjectPartKey, Integer> remainingQuotaByKey = buildRemainingQuotaMap(grouped.keySet());

        Set<Long> approvedIds = new HashSet<>();
        Set<Long> rejectedIds = new HashSet<>();
        for (Map.Entry<ProjectPartKey, List<ProjectApplication>> entry : grouped.entrySet()) {
            int quota = remainingQuotaByKey.getOrDefault(entry.getKey(), 0);
            AutoDecisionResult result = policy.decideAutomatically(entry.getValue(), quota, matchingRandom);
            approvedIds.addAll(result.approvedIds());
            rejectedIds.addAll(result.rejectedIds());
        }

        applyDecisions(applicants, approvedIds, rejectedIds, executedByMemberId);
        saveProjectApplicationPort.saveAll(applicants);

        registerApprovedMembers(applicants, approvedIds, applicantPart, executedByMemberId);

        round.executeAutoDecision(executedByMemberId);
    }

    private MatchingDecisionPolicy resolvePolicy(MatchingType type) {
        return matchingDecisionPolicies.stream()
            .filter(p -> p.supportedType() == type)
            .findFirst()
            .orElseThrow(() -> new ProjectDomainException(ProjectErrorCode.PROJECT_MATCHING_ROUND_POLICY_NOT_FOUND));
    }

    private boolean isDecidableStatus(ProjectApplicationStatus status) {
        return status == ProjectApplicationStatus.SUBMITTED
            || status == ProjectApplicationStatus.APPROVED
            || status == ProjectApplicationStatus.REJECTED;
    }

    /**
     * 지원자 전원의 챌린저 파트를 한 번의 batch 조회로 해석한다.
     * <p>
     * 한 차수 안의 모든 application 은 동일한 gisu 에 속한다는 도메인 invariant 를 활용해
     * memberId 집합으로 일괄 조회한 뒤 in-memory 매핑한다.
     */
    private Map<Long, ChallengerPart> resolveApplicantParts(List<ProjectApplication> applicants) {
        Long gisuId = applicants.get(0).getApplicationForm().getProject().getGisuId();
        Set<Long> memberIds = applicants.stream()
            .map(ProjectApplication::getApplicantMemberId)
            .collect(Collectors.toSet());
        Map<Long, ChallengerInfo> challengerByMember = getChallengerUseCase
            .batchGetByMemberIdsAndGisuId(memberIds, gisuId);

        return applicants.stream().collect(Collectors.toMap(
            ProjectApplication::getId,
            a -> challengerByMember.get(a.getApplicantMemberId()).part()
        ));
    }

    private Map<ProjectPartKey, List<ProjectApplication>> groupByProjectAndPart(
        List<ProjectApplication> applicants, Map<Long, ChallengerPart> applicantPart
    ) {
        return applicants.stream().collect(Collectors.groupingBy(
            a -> new ProjectPartKey(
                a.getApplicationForm().getProject().getId(),
                applicantPart.get(a.getId())
            )
        ));
    }

    /**
     * 본 차수에서 정책 적용 시 입력으로 사용할 (project, part) 별 남은 자리 수 맵을 한 번에 구한다.
     * <p>
     * 그룹 수만큼 DB 를 두 번씩 치던 기존 단건 호출 대신, projectId 집합 단위로 partQuota 와 active 멤버 수를
     * 일괄 조회한 뒤 in-memory 로 차감한다.
     */
    private Map<ProjectPartKey, Integer> buildRemainingQuotaMap(Set<ProjectPartKey> keys) {
        if (keys.isEmpty()) {
            return Map.of();
        }
        Set<Long> projectIds = keys.stream()
            .map(ProjectPartKey::projectId)
            .collect(Collectors.toSet());

        Map<Long, Map<ChallengerPart, Integer>> totalQuotaByProject = loadProjectPartQuotaPort
            .listByProjectIdsGroupedByProjectId(projectIds).entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> e.getValue().stream().collect(Collectors.toMap(
                    com.umc.product.project.domain.ProjectPartQuota::getPart,
                    q -> q.getQuota().intValue()
                ))
            ));

        Map<Long, Map<ChallengerPart, Long>> activeByProject = loadProjectMemberPort
            .countByProjectIdsGroupByProjectIdAndPart(projectIds);

        Map<ProjectPartKey, Integer> result = new java.util.HashMap<>();
        for (ProjectPartKey key : keys) {
            int total = totalQuotaByProject
                .getOrDefault(key.projectId(), Map.of())
                .getOrDefault(key.part(), 0);
            int active = activeByProject
                .getOrDefault(key.projectId(), Map.of())
                .getOrDefault(key.part(), 0L)
                .intValue();
            result.put(key, Math.max(0, total - active));
        }
        return result;
    }

    private void applyDecisions(
        List<ProjectApplication> applicants,
        Set<Long> approvedIds,
        Set<Long> rejectedIds,
        Long executedByMemberId
    ) {
        for (ProjectApplication app : applicants) {
            ProjectApplicationStatus target = approvedIds.contains(app.getId())
                ? ProjectApplicationStatus.APPROVED
                : ProjectApplicationStatus.REJECTED;
            if (app.getStatus() != target) {
                app.applyAutoDecision(target, executedByMemberId);
            }
        }
    }

    private void registerApprovedMembers(
        List<ProjectApplication> applicants,
        Set<Long> approvedIds,
        Map<Long, ChallengerPart> applicantPart,
        Long executedByMemberId
    ) {
        List<ProjectMember> membersToSave = applicants.stream()
            .filter(app -> approvedIds.contains(app.getId()))
            .map(app -> ProjectMember.create(
                app.getApplicationForm().getProject(),
                app.getApplicantMemberId(),
                applicantPart.get(app.getId()),
                executedByMemberId
            ))
            .toList();

        if (!membersToSave.isEmpty()) {
            saveProjectMemberPort.saveAll(membersToSave);
        }
    }

    private void validateManageAccess(Long memberId, Long chapterId) {
        if (getMemberRoleUseCase.isAdmin(memberId)) {
            return;
        }

        List<ChallengerRoleInfo> roles = getChallengerRoleUseCase.findAllByMemberId(memberId);
        boolean allowed = roles.stream()
            .anyMatch(role -> role.roleType().isAtLeastCentralCore()
                || (role.roleType() == ChallengerRoleType.CHAPTER_PRESIDENT
                && Objects.equals(role.organizationId(), chapterId)));

        if (!allowed) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_MATCHING_ROUND_ACCESS_DENIED);
        }
    }

    private record ProjectPartKey(Long projectId, ChallengerPart part) {}
}
