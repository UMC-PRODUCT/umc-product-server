package com.umc.product.llm.application.service;

import com.umc.product.llm.adapter.out.external.LlmProperties;
import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * LLM 호출 회로 차단 가드.
 * <p>
 * 연속 실패 카운터가 임계치를 넘으면 일정 시간 동안 호출을 차단한다 (in-memory).
 * 다중 인스턴스 운영 시 인스턴스 단위로 격리되며, 분산 락은 도입하지 않는다 (ADR-006 §Decision 4).
 * <p>
 * 일시적 호출 실패의 retry 는 Spring AI 자동구성된 RetryTemplate 에 위임하고,
 * 본 가드는 retry 가 모두 소진된 뒤의 최종 실패 신호만 카운팅한다.
 */
@Slf4j
@Component
public class LlmCallGuard {

    private final LlmProperties.CircuitBreaker config;
    private final Clock clock;

    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
    private final AtomicReference<Instant> skipUntil = new AtomicReference<>(Instant.EPOCH);

    public LlmCallGuard(LlmProperties properties) {
        this(properties, Clock.systemUTC());
    }

    LlmCallGuard(LlmProperties properties, Clock clock) {
        this.config = properties.circuitBreaker();
        this.clock = clock;
    }

    /**
     * @return 호출 허용 여부. 차단 상태면 false.
     */
    public boolean allow() {
        Instant now = clock.instant();
        Instant until = skipUntil.get();
        return !until.isAfter(now);
    }

    /**
     * 호출 성공 시 카운터/차단 상태를 모두 초기화한다.
     */
    public void recordSuccess() {
        consecutiveFailures.set(0);
        skipUntil.set(Instant.EPOCH);
    }

    /**
     * 호출 실패 시 카운터를 올리고, 임계치를 넘으면 차단 시각을 설정한다.
     */
    public void recordFailure() {
        int failures = consecutiveFailures.incrementAndGet();
        if (failures >= config.failureThreshold()) {
            Instant until = clock.instant().plusMillis(config.openDurationMillis());
            skipUntil.set(until);
            log.warn("LLM 호출 회로 차단: consecutiveFailures={}, skipUntil={}", failures, until);
        }
    }
}
