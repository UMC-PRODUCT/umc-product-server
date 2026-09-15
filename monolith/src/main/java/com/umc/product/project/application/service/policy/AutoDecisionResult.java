package com.umc.product.project.application.service.policy;

import java.util.Set;

/**
 * 매칭 차수 종료 시 자동 보충 결과.
 * <p>
 * 한 application id 가 두 집합에 모두 속하지는 않는다 (PM 의 기존 결정 + 정책 random 보충 합산 결과).
 *
 * @param approvedIds 합격으로 확정될 application id 집합 (PM APPROVED + 정책 random 보충)
 * @param rejectedIds 불합격으로 확정될 application id 집합 (PM REJECTED 잔여 + SUBMITTED 잔여)
 */
public record AutoDecisionResult(
    Set<Long> approvedIds,
    Set<Long> rejectedIds
) {}
