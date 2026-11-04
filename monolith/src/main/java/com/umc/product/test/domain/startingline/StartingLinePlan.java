package com.umc.product.test.domain.startingline;

import java.util.List;

import com.umc.product.test.domain.roster.RosterKey;

/**
 * 지부 하나의 출발선 PO 분류. 무프로젝트 PLAN 2명, 제출만 하는 QA 프로젝트 PO 2명, 나머지는 공개까지 하는 시딩 PO다.
 */
public record StartingLinePlan(
    String chapter,
    List<RosterKey> withoutProject,
    List<RosterKey> qaOwners,
    List<RosterKey> seededOwners
) {
    public StartingLinePlan {
        withoutProject = List.copyOf(withoutProject);
        qaOwners = List.copyOf(qaOwners);
        seededOwners = List.copyOf(seededOwners);
    }
}
