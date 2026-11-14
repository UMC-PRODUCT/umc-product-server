package com.umc.product.project.application.port.in.command;

import java.time.Instant;

/**
 * 매칭 결과 발표 처리.
 */
public interface PublishProjectMatchingResultUseCase {

    /**
     * 지원서 합불과 팀원 활성화를 반영하고 공개 완료 시각을 기록합니다.
     *
     * @return 공개를 완료했으면 true, 공개 조건을 만족하지 않으면 false
     */
    boolean publish(Long matchingRoundId);

    /** 공개 가능 여부와 조기 실행 판정에 사용한 시각을 반환합니다. */
    PublicationAttempt publishWithOutcome(Long matchingRoundId);

    record PublicationAttempt(boolean published, Instant checkedAt, Instant scheduledAt) {
        public boolean tooEarly() {
            return scheduledAt != null;
        }
    }
}
