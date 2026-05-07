package com.umc.product.llm.application.service;

import com.umc.product.llm.adapter.out.external.LlmProperties;
import java.time.Clock;
import java.util.concurrent.locks.ReentrantLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * LLM 호출 사전 페이싱을 담당하는 token bucket 기반 rate limiter.
 * <p>
 * Google AI Studio 무료 티어처럼 분당 RPM 제한이 강한 provider 환경에서, 단일 sync 가
 * 다수의 댓글을 순회하며 호출 폭주를 일으키는 것을 막는다.
 * <p>
 * {@code requestsPerMinute=0} 이면 페이싱 비활성화 (즉시 통과). 다중 인스턴스 환경에서는
 * 인스턴스 단위 격리만 보장한다 (분산 락은 도입 보류 — ADR-006 §Decision 4 참조).
 */
@Slf4j
@Component
public class LlmRateLimiter {

    private final long capacity;
    private final double refillTokensPerMs;
    private final Clock clock;

    private final ReentrantLock lock = new ReentrantLock();
    private double tokens;
    private long lastRefillTimestamp;

    @Autowired
    public LlmRateLimiter(LlmProperties properties) {
        this(properties, Clock.systemUTC());
    }

    LlmRateLimiter(LlmProperties properties, Clock clock) {
        LlmProperties.RateLimit config = properties.rateLimit();
        this.capacity = config.enabled() ? config.burst() : 0L;
        this.refillTokensPerMs = config.enabled() ? config.requestsPerMinute() / 60_000.0 : 0.0;
        this.clock = clock;
        this.tokens = capacity;
        this.lastRefillTimestamp = clock.millis();
    }

    /**
     * 토큰 1개를 소비한다. 가용 토큰이 없으면 가용까지 sleep 한다.
     * 페이싱이 비활성(capacity=0) 이면 즉시 반환.
     */
    public void acquire() {
        if (capacity == 0) {
            return;
        }
        while (true) {
            long waitMs;
            lock.lock();
            try {
                refillLocked();
                if (tokens >= 1.0) {
                    tokens -= 1.0;
                    return;
                }
                waitMs = Math.max(1L, (long) Math.ceil((1.0 - tokens) / refillTokensPerMs));
            } finally {
                lock.unlock();
            }
            log.debug("LLM rate limit 토큰 부족, {}ms 대기", waitMs);
            try {
                Thread.sleep(waitMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("LLM rate limiter 대기 중 인터럽트 발생", e);
            }
        }
    }

    private void refillLocked() {
        long now = clock.millis();
        long elapsed = now - lastRefillTimestamp;
        if (elapsed > 0) {
            tokens = Math.min(capacity, tokens + elapsed * refillTokensPerMs);
            lastRefillTimestamp = now;
        }
    }
}
