package com.umc.product.test.application.port.in.query;

import com.umc.product.test.domain.rehearsal.RoundSummary;

/**
 * 매칭 차수 하나의 지금 상태를 DB에서 직접 센 요약. 차수가 없으면 SEED-0307.
 */
public interface GetRoundSummaryUseCase {
    RoundSummary get(Long roundId);
}
