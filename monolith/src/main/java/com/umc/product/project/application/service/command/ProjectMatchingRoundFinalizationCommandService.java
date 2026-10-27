package com.umc.product.project.application.service.command;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.audit.application.port.in.annotation.Audited;
import com.umc.product.audit.domain.AuditAction;
import com.umc.product.authorization.application.port.in.query.GetChallengerRoleUseCase;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.SearchChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.challenger.application.port.in.query.dto.SearchChallengerCursorResult;
import com.umc.product.challenger.application.port.in.query.dto.SearchChallengerItemInfo;
import com.umc.product.challenger.application.port.in.query.dto.SearchChallengerQuery;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerStatus;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import com.umc.product.project.application.port.in.command.AutoDecideProjectMatchingRoundUseCase;
import com.umc.product.project.application.port.out.LoadProjectApplicationPort;
import com.umc.product.project.application.port.out.LoadProjectMatchingRoundPort;
import com.umc.product.project.application.port.out.LoadProjectMemberPort;
import com.umc.product.project.application.port.out.LoadProjectPartQuotaPort;
import com.umc.product.project.application.port.out.LoadProjectPort;
import com.umc.product.project.application.port.out.LockProjectMatchingPort;
import com.umc.product.project.application.port.out.SaveProjectApplicationPort;
import com.umc.product.project.application.port.out.SaveProjectMemberPort;
import com.umc.product.project.application.port.out.ScheduleRandomMatchingPort;
import com.umc.product.project.application.service.policy.AutoDecisionResult;
import com.umc.product.project.application.service.policy.MatchingDecisionPolicy;
import com.umc.product.project.application.service.policy.MatchingSchoolContext;
import com.umc.product.project.application.service.policy.ProjectMatchingChapterResolver;
import com.umc.product.project.application.service.policy.ProjectMatchingChapterResolver.ChapterTransferDelta;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectApplication;
import com.umc.product.project.domain.ProjectMatchingRound;
import com.umc.product.project.domain.ProjectMember;
import com.umc.product.project.domain.ProjectPartQuota;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.enums.ProjectApplicationStatus;
import com.umc.product.project.domain.enums.ProjectStatus;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 운영진 수동 실행과 스케줄러 호출을 받아 1~3차 자동 선발과 랜덤 매칭을 처리한다.
 * 차수 관리 서비스는 작업을 예약하고, 스케줄러는 이 서비스를 호출하므로 순환 의존성을 피하도록 분리한다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProjectMatchingRoundFinalizationCommandService implements
    AutoDecideProjectMatchingRoundUseCase {

    private static final int RANDOM_ASSIGNMENT_SEARCH_PAGE_SIZE = 500;
    // 랜덤 매칭 개발자 배정은 WEB_PRODUCT_ENGINEER·MOBILE_PRODUCT_ENGINEER만 대상으로 하며, 과거 개발 파트는 조회에서만 지원한다.
    private static final Set<ChallengerPart> RANDOM_ASSIGNMENT_DEVELOPER_PARTS = Set.of(
        ChallengerPart.WEB_PRODUCT_ENGINEER,
        ChallengerPart.MOBILE_PRODUCT_ENGINEER
    );

    private final LoadProjectMatchingRoundPort loadProjectMatchingRoundPort;
    private final LockProjectMatchingPort lockProjectMatchingPort;
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
    private final GetMemberUseCase getMemberUseCase;
    private final SearchChallengerUseCase searchChallengerUseCase;
    private final ProjectMatchingChapterResolver matchingChapterResolver;
    private final ScheduleRandomMatchingPort scheduleRandomMatchingPort;
    private final GetChapterUseCase getChapterUseCase;

    /**
     * 1~3차는 PM 결정 마감 후 최소 선발 인원을 보충하고, 랜덤 매칭은 같은 유형의 3차 결과 공개 완료 후 미배정 챌린저를 배정한다.
     * 발표 시각이 있는 차수의 선발 결과는 PENDING_PUBLICATION 멤버로 저장하고, 결과 공개는 발표 서비스에서 처리한다.
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
        lockProjectMatchingPort.lockByRoundId(matchingRoundId);
        ProjectMatchingRound round = loadProjectMatchingRoundPort.getByIdForUpdate(matchingRoundId);

        // 스케줄러는 실행 회원 ID 없이 호출하므로 운영진 수동 실행에만 권한을 검증한다.
        if (executedByMemberId != null) {
            validateManageAccess(executedByMemberId, round.getChapterId());
        }

        // 이미 처리한 차수는 재호출돼도 추가 선발·배정하지 않는다.
        if (round.getAutoDecisionExecutedAt() != null) {
            return;
        }

        List<ProjectMember> membersToSave;
        if (round.isRandomMatching()) {
            // 랜덤 매칭은 지원서 없이 같은 지부·유형의 3차 공개 완료 후 미배정 챌린저를 잔여 TO에 배정한다.
            Instant now = Instant.now();
            boolean thirdPublished = loadProjectMatchingRoundPort.listByChapterId(round.getChapterId()).stream()
                .anyMatch(previous -> previous.getType() == round.getType() && previous.getPhase() == MatchingPhase.THIRD
                    && previous.isAutoDecisionExecuted() && previous.isResultPublishedAt(now));
            if (!thirdPublished) {
                throw new ProjectDomainException(ProjectErrorCode.PROJECT_MATCHING_ROUND_NOT_FINALIZABLE);
            }
            membersToSave = buildRandomAssignedMembers(round, executedByMemberId);
        } else {
            // 1~3차는 PM 결정 마감 후 최소 선발 인원을 보충한다.
            if (!round.isDecisionDeadlinePassed(Instant.now())) {
                throw new ProjectDomainException(ProjectErrorCode.PROJECT_MATCHING_ROUND_NOT_FINALIZABLE);
            }
            membersToSave = selectAndApplyAutomaticDecisions(round, executedByMemberId);
        }

        if (!membersToSave.isEmpty()) {
            saveProjectMemberPort.saveAll(membersToSave);
        }
        round.executeAutoDecision(executedByMemberId);
        if (round.getPhase() == MatchingPhase.THIRD && round.getResultPublishAt() == null) {
            // 발표 시각이 없는 기존 차수는 자동 선발과 동시에 공개하므로, 커밋 후 랜덤 매칭을 예약한다.
            scheduleRandomMatchingPort.scheduleAfterPublication(round.getChapterId(), round.getType());
        }
    }

    /**
     * 1~3차 최소 선발 인원을 보충하고, 발표 시각이 없는 기존 차수는 지원서 상태를 변경·저장한다.
     * 선발된 프로젝트 멤버를 반환하며, 멤버 저장과 차수 실행 완료 기록은 호출자가 처리한다.
     */
    private List<ProjectMember> selectAndApplyAutomaticDecisions(
        ProjectMatchingRound round,
        Long executedByMemberId
    ) {
        // 매칭 유형별로 지원자 수와 잔여 모집 정원을 고려하여 최소 선발 인원을 계산한다.
        MatchingDecisionPolicy policy = resolvePolicy(round.getType());

        List<ProjectApplication> roundApplications = loadProjectApplicationPort.listByMatchingRoundId(round.getId());
        Set<Long> alreadyAssigned = roundApplications.isEmpty() ? Set.of()
            : new HashSet<>(loadProjectMemberPort.listAssignedMemberIdsByGisuId(
                roundApplications.getFirst().getApplicationForm().getProject().getGisuId()));
        if (round.getResultPublishAt() == null) {
            List<ProjectApplication> alreadyMatchedApplications = roundApplications.stream()
                .filter(a -> isDecidableStatus(a.getStatus()))
                .filter(a -> alreadyAssigned.contains(a.getApplicantMemberId())).toList();
            applyDecisions(alreadyMatchedApplications, Set.of(), executedByMemberId);
            if (!alreadyMatchedApplications.isEmpty()) {
                saveProjectApplicationPort.saveAll(alreadyMatchedApplications);
            }
        }
        List<ProjectApplication> applicants = roundApplications.stream()
            .filter(a -> isDecidableStatus(a.getStatus()))
            .filter(a -> !alreadyAssigned.contains(a.getApplicantMemberId()))
            .toList();
        if (applicants.isEmpty()) {
            return List.of();
        }

        Map<Long, ChallengerPart> partByApplicationId = resolvePartsByApplicationId(applicants);
        Set<Long> approvedMemberIds = collectApprovedMemberIds(applicants, round.getId());
        Map<ProjectPartKey, List<ProjectApplication>> grouped = groupByProjectAndPart(applicants, partByApplicationId);
        Map<ProjectPartKey, Integer> remainingQuotaByKey = buildRemainingQuotaMap(grouped.keySet());
        MatchingSchoolContext schoolContext = buildSupplementSchoolContext(applicants);

        // APPROVED는 유지하고 SUBMITTED → REJECTED 순으로 보충한다. 각 상태 안에서 학교 중복이 적은 지원자를 우선한다.
        Set<Long> approvedApplicationIds = new HashSet<>();
        for (Map.Entry<ProjectPartKey, List<ProjectApplication>> entry : grouped.entrySet()) {
            int remainingQuota = remainingQuotaByKey.getOrDefault(entry.getKey(), 0);
            // 다른 프로젝트의 합격자를 보충 선발하지 않고, 이번 차수에서 방금 선발한 지원자도 중복 배정하지 않는다.
            List<ProjectApplication> available = entry.getValue().stream()
                .filter(application -> !alreadyAssigned.contains(application.getApplicantMemberId()))
                .filter(application -> application.getStatus() == ProjectApplicationStatus.APPROVED
                    || !approvedMemberIds.contains(application.getApplicantMemberId())).toList();
            AutoDecisionResult result = policy.decideAutomatically(
                available, remainingQuota, matchingRandom, schoolContext);
            approvedApplicationIds.addAll(result.approvedIds());
            available.stream().filter(application -> result.approvedIds().contains(application.getId()))
                .forEach(application -> alreadyAssigned.add(application.getApplicantMemberId()));
        }

        // 발표 전에는 PM의 마지막 결정을 유지한다. 발표 시각이 없는 기존 차수만 APPROVED·REJECTED를 즉시 반영한다.
        if (round.getResultPublishAt() == null) {
            applyDecisions(applicants, approvedApplicationIds, executedByMemberId);
            saveProjectApplicationPort.saveAll(applicants);
        }
        return buildApprovedMembers(applicants, approvedApplicationIds, partByApplicationId, executedByMemberId);
    }

    /** 합격 지원자의 회원 ID를 모으고, 동일인의 중복 합격이 있으면 선발을 중단한다. */
    private Set<Long> collectApprovedMemberIds(List<ProjectApplication> applicants, Long matchingRoundId) {
        Set<Long> approvedMemberIds = new HashSet<>();
        for (ProjectApplication application : applicants) {
            if (application.getStatus() == ProjectApplicationStatus.APPROVED
                && !approvedMemberIds.add(application.getApplicantMemberId())) {
                log.warn("자동 선발 중복 합격 발견: matchingRoundId={}, memberId={}",
                    matchingRoundId, application.getApplicantMemberId());
                throw new ProjectDomainException(ProjectErrorCode.PROJECT_MATCHING_DUPLICATE_APPROVAL);
            }
        }
        return approvedMemberIds;
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

    /** 프로젝트 기수의 챌린저 파트를 일괄 조회해 지원서별 선발 파트를 구한다. */
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

    /** 최소 선발 인원을 각각 계산하도록 지원서를 프로젝트·파트별로 묶는다. */
    private Map<ProjectPartKey, List<ProjectApplication>> groupByProjectAndPart(
        List<ProjectApplication> applicants, Map<Long, ChallengerPart> partByApplicationId
    ) {
        return applicants.stream().collect(Collectors.groupingBy(
            a -> new ProjectPartKey(
                a.getApplicationForm().getProject().getId(),
                partByApplicationId.get(a.getId())
            ),
            // 같은 프로젝트의 파트 간에 학교 집계를 공유하므로 처리 순서를 고정한다.
            () -> new TreeMap<>(Comparator.comparing(ProjectPartKey::projectId)
                .thenComparing(key -> key.part().name())),
            Collectors.toList()
        ));
    }

    /**
     * 1~3차 선발 정책에 전달할 프로젝트·파트별 잔여 모집 정원을 일괄 계산한다.
     * PENDING_PUBLICATION도 배정된 인원이므로 ACTIVE와 함께 정원에서 차감한다.
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

    /** 발표 시각이 없는 기존 차수에 한해, 선발된 지원서는 APPROVED로 나머지는 REJECTED로 즉시 반영한다. */
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

    /** 기존 APPROVED와 자동 보충으로 선발된 지원서를 프로젝트 멤버로 만든다. */
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
     * 랜덤 매칭 미배정 챌린저를 동일 파트의 잔여 모집 정원에 배정할 멤버 목록을 만든다.
     * 후보 순서는 무작위이며, 각 후보는 같은 학교 구성원이 가장 적은 프로젝트에 배정한다.
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

        // 같은 기수에서 이미 배정된 ACTIVE·PENDING_PUBLICATION 회원은 후보에서 제외한다.
        Set<Long> alreadyMatchedMemberIds = new HashSet<>(loadProjectMemberPort.listAssignedMemberIdsByGisuId(gisuId));

        List<RandomAssignmentCandidate> candidates = new ArrayList<>(
            listActiveChallengersInChapter(gisuId, round.getChapterId())
        );
        Set<Long> candidateMemberIds = candidates.stream()
            .map(RandomAssignmentCandidate::memberId).collect(Collectors.toSet());
        MatchingSchoolContext schoolContext = buildMatchingSchoolContext(projects, candidateMemberIds);
        // 잔여 모집 정원보다 후보가 많아도 조회 순서가 배정 우선순위가 되지 않도록 섞는다.
        Collections.shuffle(candidates, matchingRandom);

        List<ProjectMember> assignments = new ArrayList<>();
        for (RandomAssignmentCandidate candidate : candidates) {
            if (remainingByKey.values().stream().noneMatch(remaining -> remaining > 0)) {
                break;
            }
            if (alreadyMatchedMemberIds.contains(candidate.memberId()) || !isRandomAssignmentPart(candidate.part(), round.getType())) {
                continue;
            }

            Optional<ProjectPartKey> targetKey = pickProjectWithSchoolPreference(
                candidate.memberId(), candidate.part(), remainingByKey, schoolContext);
            if (targetKey.isEmpty()) {
                continue;
            }

            ProjectPartKey key = targetKey.get();
            Project project = projectById.get(key.projectId());
            assignments.add(ProjectMember.createForMatching(
                project, candidate.memberId(), candidate.part(), round, executedByMemberId));
            // 다음 후보가 이번 배정까지 고려하도록 학교 집계·배정 회원·잔여 모집 정원을 즉시 갱신한다.
            schoolContext.addMember(key.projectId(), candidate.memberId());
            alreadyMatchedMemberIds.add(candidate.memberId());
            remainingByKey.computeIfPresent(key, (ignored, remaining) -> remaining - 1);
        }

        return assignments;
    }

    /**
     * 랜덤 매칭 매칭 대상 파트의 모집 정원에서 ACTIVE·PENDING_PUBLICATION 인원을 차감한다.
     * 잔여 모집 정원이 있는 프로젝트·파트만 배정 대상으로 반환한다.
     */
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
     * 매칭 지부 이동을 반영해 해당 기수·지부의 ACTIVE 챌린저 후보를 구성한다.
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
     * 조회 순서대로 배정하지 않도록 모든 페이지를 모은 뒤 호출부에서 후보 순서를 섞는다.
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
     * 동일 파트의 잔여 모집 정원이 있는 프로젝트 중 같은 학교 구성원이 가장 적은 곳을 선택한다.
     * 학교 인원수가 같으면 무작위로 선택하며, 중복을 피할 수 없어도 배정은 진행한다.
     */
    private Optional<ProjectPartKey> pickProjectWithSchoolPreference(
        Long memberId,
        ChallengerPart part,
        Map<ProjectPartKey, Integer> remainingByKey,
        MatchingSchoolContext schoolContext
    ) {
        List<ProjectPartKey> availableKeys = new ArrayList<>();
        int minimumSchoolCount = Integer.MAX_VALUE;
        for (Map.Entry<ProjectPartKey, Integer> entry : remainingByKey.entrySet()) {
            ProjectPartKey key = entry.getKey();
            if (entry.getValue() <= 0 || key.part() != part) {
                continue;
            }
            int schoolCount = schoolContext.countSameSchoolMembers(key.projectId(), memberId);
            if (schoolCount < minimumSchoolCount) {
                minimumSchoolCount = schoolCount;
                availableKeys.clear();
            }
            if (schoolCount == minimumSchoolCount) {
                availableKeys.add(key);
            }
        }
        if (availableKeys.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(availableKeys.get(matchingRandom.nextInt(availableKeys.size())));
    }

    /**
     * 1~3차 보충 선발에 사용할 학교 집계에 이번 차수 전체 파트의 APPROVED 지원자를 미리 반영한다.
     * 아직 선발 정책을 적용하지 않은 파트의 APPROVED도 유지할 인원이므로 학교 중복 판단에 포함한다.
     */
    private MatchingSchoolContext buildSupplementSchoolContext(List<ProjectApplication> applicants) {
        List<Project> projects = applicants.stream()
            .map(app -> app.getApplicationForm().getProject()).distinct().toList();
        Set<Long> applicantMemberIds = applicants.stream()
            .map(ProjectApplication::getApplicantMemberId).collect(Collectors.toSet());
        MatchingSchoolContext schoolContext = buildMatchingSchoolContext(projects, applicantMemberIds);
        applicants.stream().filter(app -> app.getStatus() == ProjectApplicationStatus.APPROVED)
            .forEach(app -> schoolContext.addMember(
                app.getApplicationForm().getProject().getId(), app.getApplicantMemberId()));
        return schoolContext;
    }

    /**
     * 후보와 기존 구성원의 학교를 일괄 조회하고, 파트 구분 없이 프로젝트별 학교 인원수를 집계한다.
     * 기존 구성원은 PM과 ACTIVE·PENDING_PUBLICATION 멤버이며, 같은 회원은 프로젝트마다 한 번만 집계한다.
     */
    private MatchingSchoolContext buildMatchingSchoolContext(Collection<Project> projects, Set<Long> candidateMemberIds) {
        Set<Long> projectIds = projects.stream().map(Project::getId).collect(Collectors.toSet());
        Map<Long, Set<Long>> assignedMemberIdsByProject = loadProjectMemberPort.listAssignedMemberIdsByProjectIds(projectIds);
        Set<Long> memberIds = new HashSet<>(candidateMemberIds);
        assignedMemberIdsByProject.values().forEach(memberIds::addAll);
        projects.forEach(project -> memberIds.add(project.getProductOwnerMemberId()));

        Map<Long, Long> schoolIdByMemberId = getMemberUseCase.findAllSchoolIdsByIds(memberIds);
        Set<Long> missingSchoolMemberIds = new HashSet<>(memberIds);
        missingSchoolMemberIds.removeAll(schoolIdByMemberId.keySet());
        if (!missingSchoolMemberIds.isEmpty()) {
            log.warn("자동 매칭 학교 정보 누락: projectIds={}, memberIds={}", projectIds, missingSchoolMemberIds);
        }
        MatchingSchoolContext schoolContext = new MatchingSchoolContext(schoolIdByMemberId);
        assignedMemberIdsByProject.forEach((projectId, assignedMemberIds) ->
            assignedMemberIds.forEach(memberId -> schoolContext.addMember(projectId, memberId)));
        projects.forEach(project -> schoolContext.addMember(project.getId(), project.getProductOwnerMemberId()));
        return schoolContext;
    }

    /**
     * 랜덤 매칭 후보 및 정원의 파트가 현재 매칭 유형의 대상인지 판단한다.
     * PLAN_DESIGN은 DESIGN, PLAN_DEVELOPER는 WEB_PRODUCT_ENGINEER·MOBILE_PRODUCT_ENGINEER만 허용한다.
     */
    private boolean isRandomAssignmentPart(ChallengerPart part, MatchingType type) {
        return switch (type) {
            case PLAN_DESIGN -> part == ChallengerPart.DESIGN;
            case PLAN_DEVELOPER -> RANDOM_ASSIGNMENT_DEVELOPER_PARTS.contains(part);
        };
    }

    /** 운영진 수동 실행 시 최고관리자·해당 기수 총괄단 또는 해당 기수·지부의 지부장인지 검증한다. */
    private void validateManageAccess(Long memberId, Long chapterId) {
        Long gisuId = getChapterUseCase.getGisuIdByChapterId(chapterId);
        if (!getChallengerRoleUseCase.isSuperAdmin(memberId)
            && !getChallengerRoleUseCase.isCentralCoreInGisu(memberId, gisuId)
            && !getChallengerRoleUseCase.isChapterPresidentInGisu(memberId, gisuId, chapterId)) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_MATCHING_ROUND_ACCESS_DENIED);
        }
    }

    private record ProjectPartKey(Long projectId, ChallengerPart part) {}

    private record RandomAssignmentCandidate(Long memberId, ChallengerPart part) {
    }
}
