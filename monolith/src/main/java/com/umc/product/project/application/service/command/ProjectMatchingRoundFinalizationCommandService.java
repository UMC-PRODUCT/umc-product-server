package com.umc.product.project.application.service.command;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.audit.application.port.in.annotation.Audited;
import com.umc.product.audit.domain.AuditAction;
import com.umc.product.authorization.application.port.in.query.GetChallengerRoleUseCase;
import com.umc.product.authorization.application.port.in.query.dto.ChallengerRoleInfo;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.SearchChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.challenger.application.port.in.query.dto.SearchChallengerCursorResult;
import com.umc.product.challenger.application.port.in.query.dto.SearchChallengerItemInfo;
import com.umc.product.challenger.application.port.in.query.dto.SearchChallengerQuery;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.common.domain.enums.ChallengerStatus;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.project.application.port.in.command.AutoDecideProjectMatchingRoundUseCase;
import com.umc.product.project.application.port.out.LoadProjectApplicationPort;
import com.umc.product.project.application.port.out.LoadProjectMatchingRoundPort;
import com.umc.product.project.application.port.out.LoadProjectMemberPort;
import com.umc.product.project.application.port.out.LoadProjectPartQuotaPort;
import com.umc.product.project.application.port.out.LoadProjectPort;
import com.umc.product.project.application.port.out.SaveProjectApplicationPort;
import com.umc.product.project.application.port.out.SaveProjectMemberPort;
import com.umc.product.project.application.service.policy.AutoDecisionResult;
import com.umc.product.project.application.service.policy.MatchingDecisionPolicy;
import com.umc.product.project.application.service.policy.ProjectMatchingChapterResolver;
import com.umc.product.project.application.service.policy.ProjectMatchingChapterResolver.ChapterTransferDelta;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectApplication;
import com.umc.product.project.domain.ProjectMatchingRound;
import com.umc.product.project.domain.ProjectMember;
import com.umc.product.project.domain.ProjectPartQuota;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.enums.ProjectApplicationStatus;
import com.umc.product.project.domain.enums.ProjectStatus;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;

import lombok.RequiredArgsConstructor;

/**
 * 1~3차 자동 선발과 4차 랜덤 매칭을 실행하는 서비스 (MATCHING-201 + 스케줄러 공통 진입점).
 * <p>
 * CRUD lifecycle (생성/수정/삭제) 은 {@link ProjectMatchingRoundCommandService} 가 책임지며,
 * 본 서비스는 1~3차 결정 마감 후의 선발과 4차 시작 시점의 랜덤 매칭을 담당한다.
 * <p>
 * CRUD 와 분리한 이유: lifecycle 호출 흐름({@code Service → Registry})과 트리거 흐름
 * ({@code Registry → Service}) 의 양 방향이 한 클래스에 모이면 순환 의존성이 발생하기 때문이다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ProjectMatchingRoundFinalizationCommandService implements
    AutoDecideProjectMatchingRoundUseCase {

    private static final int RANDOM_ASSIGNMENT_SEARCH_PAGE_SIZE = 500;
    private static final Set<ChallengerPart> DEVELOPER_PARTS = Set.of(
        ChallengerPart.WEB_PRODUCT_ENGINEER,
        ChallengerPart.MOBILE_PRODUCT_ENGINEER
    );

    private final LoadProjectMatchingRoundPort loadProjectMatchingRoundPort;
    private final LoadProjectApplicationPort loadProjectApplicationPort;
    private final SaveProjectApplicationPort saveProjectApplicationPort;
    private final LoadProjectPort loadProjectPort;
    private final LoadProjectPartQuotaPort loadProjectPartQuotaPort;
    private final LoadProjectMemberPort loadProjectMemberPort;
    private final SaveProjectMemberPort saveProjectMemberPort;
    private final List<MatchingDecisionPolicy> matchingDecisionPolicies;
    private final Random matchingRandom;
    private final GetChallengerRoleUseCase getChallengerRoleUseCase;
    private final GetChallengerUseCase getChallengerUseCase;
    private final SearchChallengerUseCase searchChallengerUseCase;
    private final ProjectMatchingChapterResolver matchingChapterResolver;

    /**
     * 1~3차는 PM 결정 마감 후 최소 선발 인원을 보충하고, 4차(랜덤 매칭)는 시작 시각부터 미배정 챌린저를 배정한다.
     * 발표 시각이 있는 차수의 선발 결과는 발표 대기 멤버로 저장하고, 결과 공개는 발표 서비스에서 처리한다.
     */
    @Audited(
        domain = Domain.PROJECT,
        action = AuditAction.FINALIZE,
        targetType = "ProjectMatchingRound",
        targetId = "#matchingRoundId",
        description = "'프로젝트 매칭 라운드를 자동 확정했습니다.'"
    )
    @Override
    public void autoDecide(Long matchingRoundId, Long executedByMemberId) {
        // 같은 차수의 동시 실행으로 중복 배정되지 않도록 잠금 조회
        ProjectMatchingRound round = loadProjectMatchingRoundPort.getByIdForUpdate(matchingRoundId);

        // executedByMemberId == null 은 스케줄러 호출. 권한 검증은 운영진 수동 호출에만 적용.
        if (executedByMemberId != null) {
            validateManageAccess(executedByMemberId, round.getChapterId());
        }

        // 자동 선발 또는 랜덤 매칭이 이미 실행됐다면 중복 처리하지 않음
        if (round.getAutoDecisionExecutedAt() != null) {
            return;
        }

        // 4차(랜덤 매칭)는 시작 시각부터 미배정 챌린저를 잔여 정원에 배정
        if (round.isRandomMatching()) {
            if (Instant.now().isBefore(round.getStartsAt())) {
                throw new ProjectDomainException(ProjectErrorCode.PROJECT_MATCHING_ROUND_NOT_FINALIZABLE);
            }
            List<ProjectMember> assignments = buildRandomAssignedMembers(round, executedByMemberId);
            if (!assignments.isEmpty()) {
                saveProjectMemberPort.saveAll(assignments);
            }
            round.executeAutoDecision(executedByMemberId);
            // 4차는 지원서를 사용하지 않으므로 아래의 1~3차 선발 처리를 건너뜀
            return;
        }

        // 1~3차 최소 선발 인원 보충은 PM의 합/불 결정 마감 이후에만 실행
        if (!round.isDecisionDeadlinePassed(Instant.now())) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_MATCHING_ROUND_NOT_FINALIZABLE);
        }

        // 디자인과 개발자 매칭은 최소 선발 인원 계산 기준이 다르므로 유형에 맞는 정책 선택
        MatchingDecisionPolicy policy = resolvePolicy(round.getType());

        List<ProjectApplication> applicants = loadProjectApplicationPort
            .listByMatchingRoundId(matchingRoundId).stream()
            .filter(a -> isDecidableStatus(a.getStatus()))
            .toList();

        List<ProjectMember> membersToSave = new ArrayList<>();
        if (!applicants.isEmpty()) {
            Map<Long, ChallengerPart> partByApplicationId = resolvePartsByApplicationId(applicants);
            Map<ProjectPartKey, List<ProjectApplication>> grouped = groupByProjectAndPart(applicants, partByApplicationId);

            Map<ProjectPartKey, Integer> remainingQuotaByKey = buildRemainingQuotaMap(grouped.keySet());

            // 기존 합격을 유지하고 SUBMITTED → REJECTED 순으로 부족한 인원을 보충한 지원서 ID 수집
            Set<Long> approvedApplicationIds = new HashSet<>();
            for (Map.Entry<ProjectPartKey, List<ProjectApplication>> entry : grouped.entrySet()) {
                int remainingQuota = remainingQuotaByKey.getOrDefault(entry.getKey(), 0);
                AutoDecisionResult result = policy.decideAutomatically(entry.getValue(), remainingQuota, matchingRandom);
                approvedApplicationIds.addAll(result.approvedIds());
            }

            // 발표 시각이 없는 기존 차수만 지원서에 합/불을 즉시 반영
            // 발표 시각이 있으면 PM의 마지막 결정을 유지하고, 최종 합/불은 결과 발표 시 반영
            if (round.getResultPublishAt() == null) {
                applyDecisions(applicants, approvedApplicationIds, executedByMemberId);
                saveProjectApplicationPort.saveAll(applicants);
            }
            membersToSave.addAll(buildApprovedMembers(applicants, approvedApplicationIds, partByApplicationId, executedByMemberId));
        }

        if (!membersToSave.isEmpty()) {
            saveProjectMemberPort.saveAll(membersToSave);
        }

        round.executeAutoDecision(executedByMemberId);
    }

    /** 디자인·개발자 매칭 유형에 해당하는 최소 선발 정책을 반환한다. */
    private MatchingDecisionPolicy resolvePolicy(MatchingType type) {
        return matchingDecisionPolicies.stream()
            .filter(p -> p.supportedType() == type)
            .findFirst()
            .orElseThrow(() -> new ProjectDomainException(ProjectErrorCode.PROJECT_MATCHING_ROUND_POLICY_NOT_FOUND));
    }

    /** 기존 합격 유지와 미결정·불합격 지원자 보충에 사용할 지원서 상태인지 판단한다. */
    private boolean isDecidableStatus(ProjectApplicationStatus status) {
        return status == ProjectApplicationStatus.SUBMITTED
            || status == ProjectApplicationStatus.APPROVED
            || status == ProjectApplicationStatus.REJECTED;
    }

    /** 지원자의 파트를 일괄 조회해, 프로젝트·파트별 선발에 사용할 지원서 ID → 파트 맵을 반환한다. */
    private Map<Long, ChallengerPart> resolvePartsByApplicationId(List<ProjectApplication> applicants) {
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

    /** 파트별 잔여 정원에 맞춰 선발 정책을 적용할 수 있도록 지원서를 프로젝트·파트 단위로 묶는다. */
    private Map<ProjectPartKey, List<ProjectApplication>> groupByProjectAndPart(
        List<ProjectApplication> applicants, Map<Long, ChallengerPart> partByApplicationId
    ) {
        return applicants.stream().collect(Collectors.groupingBy(
            a -> new ProjectPartKey(
                a.getApplicationForm().getProject().getId(),
                partByApplicationId.get(a.getId())
            )
        ));
    }

    /**
     * 1~3차 지원서가 있는 프로젝트·파트의 정원과 기존 배정 인원을 일괄 조회해 잔여 정원을 계산한다.
     * 아직 발표되지 않은 배정도 자리를 차지하므로 ACTIVE·PENDING_PUBLICATION을 함께 차감한다.
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
                    ProjectPartQuota::getPart,
                    q -> q.getQuota().intValue()
                ))
            ));

        Map<Long, Map<ChallengerPart, Long>> assignedByProject = loadProjectMemberPort
            .countAssignedByProjectIdsGroupByProjectIdAndPart(projectIds);

        Map<ProjectPartKey, Integer> result = new HashMap<>();
        for (ProjectPartKey key : keys) {
            int total = totalQuotaByProject
                .getOrDefault(key.projectId(), Map.of())
                .getOrDefault(key.part(), 0);
            int assigned = assignedByProject
                .getOrDefault(key.projectId(), Map.of())
                .getOrDefault(key.part(), 0L)
                .intValue();
            result.put(key, Math.max(0, total - assigned));
        }
        return result;
    }

    /** 발표 시각이 없는 기존 차수에 한해, 선발된 지원서는 합격으로 나머지는 불합격으로 즉시 반영한다. */
    private void applyDecisions(
        List<ProjectApplication> applicants,
        Set<Long> approvedApplicationIds,
        Long executedByMemberId
    ) {
        for (ProjectApplication app : applicants) {
            ProjectApplicationStatus target = approvedApplicationIds.contains(app.getId())
                ? ProjectApplicationStatus.APPROVED
                : ProjectApplicationStatus.REJECTED;
            if (app.getStatus() != target) {
                app.applyAutoDecision(target, executedByMemberId);
            }
        }
    }

    /**
     * 합격 대상으로 결정한 지원서에 연결된 프로젝트 멤버를 생성한다.
     * 발표 시각이 있으면 PENDING_PUBLICATION, 없으면 ACTIVE 상태로 생성하며 저장은 호출자가 수행한다.
     */
    private List<ProjectMember> buildApprovedMembers(
        List<ProjectApplication> applicants,
        Set<Long> approvedApplicationIds,
        Map<Long, ChallengerPart> partByApplicationId,
        Long executedByMemberId
    ) {
        return applicants.stream()
            .filter(app -> approvedApplicationIds.contains(app.getId()))
            .map(app -> ProjectMember.createFromApplication(
                app,
                partByApplicationId.get(app.getId()),
                executedByMemberId
            ))
            .toList();
    }

    /**
     * 4차의 미배정 후보를 동일 파트의 잔여 정원이 있는 프로젝트에 랜덤 배정한다.
     * 후보 처리 순서와 배정할 프로젝트를 랜덤으로 선택하며, 생성한 멤버는 호출자가 일괄 저장한다.
     */
    private List<ProjectMember> buildRandomAssignedMembers(
        ProjectMatchingRound round,
        Long executedByMemberId
    ) {
        List<Project> projects = loadProjectPort.listByChapterIdAndStatus(
            round.getChapterId(),
            ProjectStatus.IN_PROGRESS
        );
        if (projects.isEmpty()) {
            return List.of();
        }

        Map<Long, Project> projectById = projects.stream()
            .collect(Collectors.toMap(Project::getId, project -> project));
        Long gisuId = projects.getFirst().getGisuId();

        Map<ProjectPartKey, Integer> remainingByKey = buildRemainingMatchingQuotaMap(projects, round.getType());
        if (remainingByKey.values().stream().noneMatch(remaining -> remaining > 0)) {
            return List.of();
        }

        // 다른 차수·프로젝트에서 발표 대기 중인 배정도 포함해 중복 배정을 방지
        Set<Long> alreadyMatchedMemberIds = new HashSet<>(loadProjectMemberPort.listAssignedMemberIdsByGisuId(gisuId));

        List<RandomAssignmentCandidate> candidates = new ArrayList<>(
            listActiveChallengersInChapter(gisuId, round.getChapterId())
        );
        Collections.shuffle(candidates, matchingRandom);

        List<ProjectMember> assignments = new ArrayList<>();
        for (RandomAssignmentCandidate candidate : candidates) {
            if (remainingByKey.values().stream().noneMatch(remaining -> remaining > 0)) {
                break;
            }
            if (alreadyMatchedMemberIds.contains(candidate.memberId()) || !isRandomAssignmentPart(candidate.part(), round.getType())) {
                continue;
            }

            Optional<ProjectPartKey> targetKey = pickRandomRemainingProjectKey(candidate.part(), remainingByKey);
            if (targetKey.isEmpty()) {
                continue;
            }

            ProjectPartKey key = targetKey.get();
            Project project = projectById.get(key.projectId());
            assignments.add(ProjectMember.createForMatching(
                project, candidate.memberId(), candidate.part(), round, executedByMemberId));
            alreadyMatchedMemberIds.add(candidate.memberId());
            remainingByKey.computeIfPresent(key, (ignored, remaining) -> remaining - 1);
        }

        return assignments;
    }

    /** 4차 매칭 대상 파트의 잔여 정원을 계산하고, 빈자리가 있는 프로젝트·파트만 반환한다. */
    private Map<ProjectPartKey, Integer> buildRemainingMatchingQuotaMap(
        List<Project> projects, MatchingType type
    ) {
        Set<Long> projectIds = projects.stream()
            .map(Project::getId)
            .collect(Collectors.toSet());
        Map<Long, Map<ChallengerPart, Long>> assignedByProject = loadProjectMemberPort
            .countAssignedByProjectIdsGroupByProjectIdAndPart(projectIds);

        Map<ProjectPartKey, Integer> result = new HashMap<>();
        Map<Long, List<ProjectPartQuota>> quotasByProject = loadProjectPartQuotaPort
            .listByProjectIdsGroupedByProjectId(projectIds);
        for (Project project : projects) {
            Long projectId = project.getId();
            for (ProjectPartQuota quota : quotasByProject.getOrDefault(projectId, List.of())) {
                ChallengerPart part = quota.getPart();
                if (!isRandomAssignmentPart(part, type)) {
                    continue;
                }

                ProjectPartKey key = new ProjectPartKey(projectId, part);
                int assigned = assignedByProject
                    .getOrDefault(projectId, Map.of())
                    .getOrDefault(part, 0L)
                    .intValue();
                int remaining = Math.max(0, quota.getQuota().intValue() - assigned);
                if (remaining > 0) {
                    result.put(key, remaining);
                }
            }
        }
        return result;
    }

    /**
     * 해당 기수·지부의 ACTIVE 챌린저를 랜덤 배정 후보로 구성한다.
     * 학교 기준 지부 목록에서 전출자를 제외하고 전입자를 추가하며, 같은 회원은 한 번만 포함한다.
     */
    private List<RandomAssignmentCandidate> listActiveChallengersInChapter(Long gisuId, Long chapterId) {
        ChapterTransferDelta delta = matchingChapterResolver.deltaOf(chapterId);

        List<RandomAssignmentCandidate> candidates = new ArrayList<>();
        Set<Long> addedMemberIds = new HashSet<>();
        for (SearchChallengerItemInfo info : searchActiveChallengersBySchoolChapter(gisuId, chapterId)) {
            if (!delta.outboundMemberIds().contains(info.memberId()) && addedMemberIds.add(info.memberId())) {
                candidates.add(new RandomAssignmentCandidate(info.memberId(), info.part()));
            }
        }
        for (ChallengerInfo inbound : delta.inboundChallengers()) {
            if (inbound.challengerStatus() == ChallengerStatus.ACTIVE && addedMemberIds.add(inbound.memberId())) {
                candidates.add(new RandomAssignmentCandidate(inbound.memberId(), inbound.part()));
            }
        }
        return candidates;
    }

    /**
     * 해당 기수의 학교 기준 지부에 속한 ACTIVE 챌린저를 조회한다.
     * 전체 후보를 모아 셔플하므로 마지막 커서까지 모든 페이지를 조회한다.
     */
    private List<SearchChallengerItemInfo> searchActiveChallengersBySchoolChapter(Long gisuId, Long chapterId) {
        SearchChallengerQuery query = new SearchChallengerQuery(
            null,
            null,
            null,
            null,
            null,
            chapterId,
            null,
            gisuId,
            List.of(ChallengerStatus.ACTIVE)
        );

        List<SearchChallengerItemInfo> result = new ArrayList<>();
        Long cursor = null;
        boolean hasNext;
        do {
            SearchChallengerCursorResult page = searchChallengerUseCase.cursorSearch(
                query,
                cursor,
                RANDOM_ASSIGNMENT_SEARCH_PAGE_SIZE
            );
            result.addAll(page.content());
            cursor = page.nextCursor();
            hasNext = page.hasNext() && cursor != null;
        } while (hasNext);
        return result;
    }

    /**
     * 후보와 동일 파트의 잔여 정원이 있는 프로젝트 중 하나를 같은 확률로 선택한다.
     * 배정 가능한 프로젝트가 없으면 빈 Optional을 반환한다.
     */
    private Optional<ProjectPartKey> pickRandomRemainingProjectKey(
        ChallengerPart part,
        Map<ProjectPartKey, Integer> remainingByKey
    ) {
        List<ProjectPartKey> availableKeys = remainingByKey.entrySet().stream()
            .filter(entry -> entry.getValue() > 0)
            .filter(entry -> entry.getKey().part() == part)
            .map(Map.Entry::getKey)
            .toList();

        if (availableKeys.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(availableKeys.get(matchingRandom.nextInt(availableKeys.size())));
    }

    /**
     * 4차 후보 및 정원의 파트가 현재 매칭 유형의 대상인지 판단한다.
     * PLAN_DESIGN은 DESIGN, PLAN_DEVELOPER는 WEB_PRODUCT_ENGINEER·MOBILE_PRODUCT_ENGINEER만 허용한다.
     */
    private boolean isRandomAssignmentPart(ChallengerPart part, MatchingType type) {
        return switch (type) {
            case PLAN_DESIGN -> part == ChallengerPart.DESIGN;
            case PLAN_DEVELOPER -> DEVELOPER_PARTS.contains(part);
        };
    }

    /** 운영진 수동 실행 시 최고관리자·중앙 총괄단 이상 또는 해당 지부장인지 검증한다. */
    private void validateManageAccess(Long memberId, Long chapterId) {
        if (getChallengerRoleUseCase.isSuperAdmin(memberId)) {
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

    private record RandomAssignmentCandidate(Long memberId, ChallengerPart part) {
    }
}
