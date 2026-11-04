package com.umc.product.test.domain.rehearsal;

/**
 * 봇이 사람·프로젝트·지원서를 건너뛴 이유. 응답의 skipped에 사유별로 센다.
 */
public enum SkipReason {
    QA_RESERVED,
    EXCLUDED,
    HAS_TEAM,
    NOT_SELECTED,
    ALREADY_APPLIED,
    NO_ELIGIBLE_PROJECT,
    QA_PROJECT,
    ALREADY_DECIDED
}
