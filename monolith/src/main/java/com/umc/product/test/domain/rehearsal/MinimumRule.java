package com.umc.product.test.domain.rehearsal;

/**
 * 자동 선발의 최소 선발 인원. 차수 유형을 묶은 운영 정책 조회다.
 */
@FunctionalInterface
public interface MinimumRule {
    int minimumRequired(int applicants, int remainingQuota);
}
