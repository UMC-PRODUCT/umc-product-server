package com.umc.product.project.application.service.policy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 자동 선발·배정에서 학교 중복을 판단하는 데 필요한 회원별 학교와 프로젝트별 집계 상태를 관리한다.
 * 파트와 관계없이 집계하고, PO·기존 구성원·APPROVED 지원자에 같은 회원이 겹쳐도 한 번만 반영한다.
 * 한 차수의 자동 매칭 실행 동안 공유하며, 선발할 때마다 갱신해 다음 선택에 반영한다.
 */
public class MatchingSchoolContext {

    private final Map<Long, Long> schoolIdByMemberId;
    private final Map<Long, Set<Long>> memberIdsByProject = new HashMap<>();
    private final Map<Long, Map<Long, Integer>> schoolCountsByProject = new HashMap<>();

    public MatchingSchoolContext(Map<Long, Long> schoolIdByMemberId) {
        this.schoolIdByMemberId = Map.copyOf(schoolIdByMemberId);
    }

    /** 후보와 같은 학교의 프로젝트 구성원이 몇 명인지 반환한다. 후보의 학교 정보가 없으면 0을 반환한다. */
    public int countSameSchoolMembers(Long projectId, Long memberId) {
        Long schoolId = schoolIdByMemberId.get(memberId);
        if (schoolId == null) {
            return 0;
        }
        return schoolCountsByProject.getOrDefault(projectId, Map.of()).getOrDefault(schoolId, 0);
    }

    /** 기존 구성원과 선발된 회원을 반영해, 다음 선택에서도 최신 학교 인원수를 사용한다. */
    public void addMember(Long projectId, Long memberId) {
        Long schoolId = schoolIdByMemberId.get(memberId);
        // 학교 정보가 없는 회원은 학교별 인원 집계에서 제외한다.
        if (schoolId == null) {
            return;
        }
        if (memberIdsByProject.computeIfAbsent(projectId, ignored -> new HashSet<>()).add(memberId)) {
            schoolCountsByProject.computeIfAbsent(projectId, ignored -> new HashMap<>())
                .merge(schoolId, 1, Integer::sum);
        }
    }
}
