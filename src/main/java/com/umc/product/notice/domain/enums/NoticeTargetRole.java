package com.umc.product.notice.domain.enums;

import java.util.Set;

/**
 * 공지 대상 역할 유형.
 * CHALLENGER는 일반 챌린저 공지, 나머지는 운영진 공지 대상을 나타냅니다.
 */

public enum NoticeTargetRole {
    CHALLENGER,             // 일반 챌린저 공지
    SCHOOL_PART_LEADER,     // 교내파트장
    SCHOOL_PRESIDENT_TEAM,  // 교내회장단
    CENTRAL_EDUCATION_TEAM, // 중앙운영진 교육국
    CENTRAL_OPERATING_TEAM; // 중앙운영진 운영국

    public boolean isCentralRole() {
        return this == CENTRAL_EDUCATION_TEAM || this == CENTRAL_OPERATING_TEAM;
    }

    public boolean isSchoolRole() {
        return this == SCHOOL_PART_LEADER || this == SCHOOL_PRESIDENT_TEAM;
    }

    /**
     * 해당 역할이 읽을 수 있는 공지 대상 역할 목록.
     * 작성 권한이 있는 대상의 공지까지 조회 가능합니다.
     */
    public Set<NoticeTargetRole> readableRoles() {
        return switch (this) {
            case CENTRAL_EDUCATION_TEAM -> Set.of(CENTRAL_EDUCATION_TEAM, SCHOOL_PART_LEADER, SCHOOL_PRESIDENT_TEAM);
            case CENTRAL_OPERATING_TEAM -> Set.of(CENTRAL_OPERATING_TEAM, SCHOOL_PART_LEADER, SCHOOL_PRESIDENT_TEAM);
            case SCHOOL_PRESIDENT_TEAM -> Set.of(SCHOOL_PRESIDENT_TEAM, SCHOOL_PART_LEADER);
            case SCHOOL_PART_LEADER -> Set.of(SCHOOL_PART_LEADER);
            case CHALLENGER -> Set.of(CHALLENGER);
        };
    }
}
