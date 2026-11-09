package com.umc.product.project.application.port.out.dto;

import java.time.Instant;

/**
 * 차수 하나에 등록된 자동 선발·결과 발표 알람 시각. 등록하지 않은 알람은 null이다.
 */
public record MatchingRoundTimers(Long roundId, Instant autoDecideAt, Instant publishAt) {

    public static MatchingRoundTimers none(Long roundId) {
        return new MatchingRoundTimers(roundId, null, null);
    }
}
