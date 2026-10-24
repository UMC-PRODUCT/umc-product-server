package com.umc.product.project.application.service.policy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.umc.product.project.domain.ProjectApplication;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.enums.ProjectApplicationStatus;

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
     *   <li>APPROVED 지원자 수가 정책 최소치 미만이면 SUBMITTED 풀에서 학교 중복이 적은 순으로 보충</li>
     *   <li>SUBMITTED 풀이 부족하면 REJECTED 풀에서 같은 기준으로 추가 보충 (PM 결정 override)</li>
     *   <li>그 외 SUBMITTED / REJECTED 는 모두 REJECTED 로 확정</li>
     * </ol>
     * 학교 컨텍스트에는 기존 구성원과 이번 차수 전체 파트의 APPROVED 지원자가 미리 반영되어 있어야 한다.
     * 선발할 때마다 전달받은 컨텍스트를 갱신하며, 호출자는 다음 파트의 선발에도 같은 객체를 사용한다.
     */
    default AutoDecisionResult decideAutomatically(
        List<ProjectApplication> applicants, int quota, Random random, MatchingSchoolContext schoolContext
    ) {
        Set<Long> approvedIds = new HashSet<>();
        Set<Long> rejectedIds = new HashSet<>();

        List<ProjectApplication> alreadyApproved = filterByStatus(applicants, ProjectApplicationStatus.APPROVED);
        alreadyApproved.forEach(a -> approvedIds.add(a.getId()));

        int required = minimumRequired(applicants.size(), quota);
        int needed = Math.max(0, required - alreadyApproved.size());

        List<ProjectApplication> submittedPool = filterByStatus(applicants, ProjectApplicationStatus.SUBMITTED);
        int pickFromSubmitted = Math.min(needed, submittedPool.size());
        pickWithSchoolPreference(submittedPool, pickFromSubmitted, random, schoolContext, approvedIds, rejectedIds);

        int remainingNeeded = needed - pickFromSubmitted;
        List<ProjectApplication> rejectedPool = filterByStatus(applicants, ProjectApplicationStatus.REJECTED);
        pickWithSchoolPreference(rejectedPool, remainingNeeded, random, schoolContext, approvedIds, rejectedIds);

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
     * 같은 학교 구성원이 적은 지원자부터 선발하고, 선발 직후 집계에 반영한다.
     * 먼저 셔플해 학교 인원수가 같은 지원자는 무작위 순서로 선택한다.
     */
    private static void pickWithSchoolPreference(
        List<ProjectApplication> pool,
        int pickCount,
        Random random,
        MatchingSchoolContext schoolContext,
        Set<Long> approvedIds,
        Set<Long> rejectedIds
    ) {
        List<ProjectApplication> candidates = new ArrayList<>(pool);
        Collections.shuffle(candidates, random);
        int count = Math.min(pickCount, candidates.size());
        for (int i = 0; i < count; i++) {
            int selectedIndex = 0;
            int minimumSchoolCount = Integer.MAX_VALUE;
            for (int j = 0; j < candidates.size(); j++) {
                ProjectApplication candidate = candidates.get(j);
                int schoolCount = schoolContext.countSameSchoolMembers(
                    candidate.getApplicationForm().getProject().getId(), candidate.getApplicantMemberId());
                if (schoolCount < minimumSchoolCount) {
                    minimumSchoolCount = schoolCount;
                    selectedIndex = j;
                }
            }
            ProjectApplication selected = candidates.remove(selectedIndex);
            approvedIds.add(selected.getId());
            schoolContext.addMember(selected.getApplicationForm().getProject().getId(), selected.getApplicantMemberId());
        }
        candidates.forEach(candidate -> rejectedIds.add(candidate.getId()));
    }
}
