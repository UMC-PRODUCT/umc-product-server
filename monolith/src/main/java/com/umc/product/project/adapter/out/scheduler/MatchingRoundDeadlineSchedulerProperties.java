package com.umc.product.project.adapter.out.scheduler;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 매칭 차수 자동 선발 스케줄러 설정.
 *
 * @param deadlineBufferMinutes 자동 선발 task 를 decisionDeadline 이후로 지연할 시간. 환경변수 단위는 minute.
 */
@ConfigurationProperties(prefix = "app.project.matching-round")
public record MatchingRoundDeadlineSchedulerProperties(
    Long deadlineBufferMinutes
) {

    private static final long DEFAULT_DEADLINE_BUFFER_MINUTES = 1L;

    public MatchingRoundDeadlineSchedulerProperties {
        if (deadlineBufferMinutes == null) {
            deadlineBufferMinutes = DEFAULT_DEADLINE_BUFFER_MINUTES;
        }
        if (deadlineBufferMinutes < 1) {
            throw new IllegalArgumentException("deadlineBufferMinutes must be greater than or equal to 1");
        }
    }

    public Duration deadlineBuffer() {
        return Duration.ofMinutes(deadlineBufferMinutes);
    }
}
