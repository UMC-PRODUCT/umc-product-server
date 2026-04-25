package com.umc.product.notice.domain.enums;

/**
 * 공지 대상 역할 유형. CHALLENGER는 일반 챌린저 공지를 의미하며, 나머지는 운영진 공지 대상을 나타냅니다.
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
}
