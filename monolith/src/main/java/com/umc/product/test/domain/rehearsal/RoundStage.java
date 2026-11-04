package com.umc.product.test.domain.rehearsal;

/**
 * 차수 시각과 실행 여부로 본 지금 단계. 1~3차는 위 여섯 단계, 랜덤 매칭은 아래 네 단계와 PUBLISHED를 쓴다.
 */
public enum RoundStage {
    BEFORE_START,
    APPLICATION_OPEN,
    DECISION_WINDOW,
    AWAITING_AUTO_DECISION,
    AWAITING_PUBLISH,
    AWAITING_ASSIGNMENT,
    ASSIGNED,
    ADJUSTMENT_OPEN,
    ADJUSTMENT_CLOSED,
    PUBLISHED
}
