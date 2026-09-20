package com.umc.product.project.application.service.command;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 매칭 차수 생성/수정 검증 설정.
 *
 * @param minPhaseIntervalMinutes FIRST, SECOND, THIRD 차수 사이 최소 간격. 환경변수 단위는 minute.
 */
@ConfigurationProperties(prefix = "app.project.matching-round")
public record ProjectMatchingRoundProperties(
    Long minPhaseIntervalMinutes
) {

    private static final long DEFAULT_MIN_PHASE_INTERVAL_MINUTES = 1L;

    public ProjectMatchingRoundProperties {
        if (minPhaseIntervalMinutes == null) {
            minPhaseIntervalMinutes = DEFAULT_MIN_PHASE_INTERVAL_MINUTES;
        }
        if (minPhaseIntervalMinutes < 1) {
            throw new IllegalArgumentException("minPhaseIntervalMinutes must be greater than or equal to 1");
        }
    }

    public Duration minPhaseInterval() {
        return Duration.ofMinutes(minPhaseIntervalMinutes);
    }
}
