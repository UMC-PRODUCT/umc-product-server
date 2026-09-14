package com.umc.product.project.application.service.policy;

import com.umc.product.project.domain.ProjectApplication;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.enums.ProjectApplicationStatus;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * 매칭 차수 종료 시 자동 보충 정책.
 * <p>
 * Strategy 패턴 — {@link MatchingType} 별로 구현체가 다르며 Spring 이 {@link #supportedType()} 으로 매핑한다.
 * <p>
 * 알고리즘 전체는 {@link #decideAutomatically} default 메서드가 책임지고, 구현체는
 * {@link #minimumRequired} (최소 합격 인원 계산) 만 채우면 된다.
 */
public interface MatchingDecisionPolicy {

    /** 본 정책이 적용되는 매칭 종류. */
    MatchingType supportedType();

    /**
     * 정책상 최소 합격 인원.
     *
     * @param applicantsCount 본 차수 본 프로젝트의 전체 지원자 수 (status 무관, CANCELLED 제외)
     * @param quota           본 프로젝트의 해당 파트 정원
     * @return 최소 합격 인원 (정책 의무 없음 → 0)
     */
    int minimumRequired(int applicantsCount, int quota);

    /**
     * 차수 종료 시 자동 보충 알고리즘 (final-api.md §3 매트릭스 ③).
     * <p>
     * <ol>
     *   <li>이미 APPROVED 인 지원자는 그대로 합격 유지</li>
     *   <li>합격 카운트가 정책 최소치 미만이면 SUBMITTED 풀에서 random 보충</li>
     *   <li>SUBMITTED 풀이 부족하면 REJECTED 풀에서 추가 random 보충 (PM 결정 override)</li>
     *   <li>그 외 SUBMITTED / REJECTED 는 모두 REJECTED 로 확정</li>
     * </ol>
     */
    default AutoDecisionResult decideAutomatically(
        List<ProjectApplication> applicants, int quota, Random random
    ) {
        Set<Long> approvedIds = new HashSet<>();
        Set<Long> rejectedIds = new HashSet<>();

        List<ProjectApplication> alreadyApproved = filterByStatus(applicants, ProjectApplicationStatus.APPROVED);
        alreadyApproved.forEach(a -> approvedIds.add(a.getId()));

        int required = minimumRequired(applicants.size(), quota);
        int needed = Math.max(0, required - alreadyApproved.size());

        List<ProjectApplication> submittedPool = filterByStatus(applicants, ProjectApplicationStatus.SUBMITTED);
        int pickFromSubmitted = Math.min(needed, submittedPool.size());
        randomPick(submittedPool, pickFromSubmitted, random, approvedIds, rejectedIds);

        int remainingNeeded = needed - pickFromSubmitted;
        List<ProjectApplication> rejectedPool = filterByStatus(applicants, ProjectApplicationStatus.REJECTED);
        randomPick(rejectedPool, remainingNeeded, random, approvedIds, rejectedIds);

        return new AutoDecisionResult(approvedIds, rejectedIds);
    }

    private static List<ProjectApplication> filterByStatus(
        List<ProjectApplication> applicants, ProjectApplicationStatus status
    ) {
        return applicants.stream()
            .filter(a -> a.getStatus() == status)
            .toList();
    }

    /**
     * pool 을 셔플한 뒤 앞에서 {@code pickCount} 개를 approvedIds 로, 나머지는 rejectedIds 로 분류한다.
     */
    private static void randomPick(
        List<ProjectApplication> pool,
        int pickCount,
        Random random,
        Set<Long> approvedIds,
        Set<Long> rejectedIds
    ) {
        if (pool.isEmpty()) {
            return;
        }
        List<ProjectApplication> shuffled = new ArrayList<>(pool);
        Collections.shuffle(shuffled, random);
        for (int i = 0; i < shuffled.size(); i++) {
            Long id = shuffled.get(i).getId();
            if (i < pickCount) {
                approvedIds.add(id);
            } else {
                rejectedIds.add(id);
            }
        }
    }
}
