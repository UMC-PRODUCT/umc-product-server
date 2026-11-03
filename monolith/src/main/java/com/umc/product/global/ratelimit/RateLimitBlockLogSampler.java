package com.umc.product.global.ratelimit;

import java.time.Duration;
import java.util.OptionalLong;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;

/**
 * 차단 로그를 limiter key마다 한 구간에 한 줄로 줄인다.
 *
 * <p>공격성 트래픽이 차단될 때마다 WARN을 남기면 차단된 요청 수만큼 로그가 쌓여 Loki가 OOM에 빠졌다.
 * 구간 안의 나머지 차단은 개수만 세고, 다음 구간의 첫 줄에 그 개수를 함께 남긴다.
 * 정확한 차단 총량은 {@link ApiRateLimitMetrics}의 blocked counter가 맡는다.
 *
 * <p>출발지 IP가 많아도 이 cache가 메모리 누수가 되지 않도록 항목 수 상한과 만료를 둔다.
 * 만료되거나 밀려난 key의 남은 개수는 로그에 남지 않는다.
 */
public class RateLimitBlockLogSampler {

    private static final Duration LOG_INTERVAL = Duration.ofMinutes(1);
    private static final Duration IDLE_EXPIRY = Duration.ofMinutes(10);
    private static final long MAXIMUM_KEYS = 10_000;

    private final Ticker ticker;
    private final Cache<String, Window> windows;

    public RateLimitBlockLogSampler(Ticker ticker) {
        this.ticker = ticker;
        this.windows = Caffeine.newBuilder()
            .maximumSize(MAXIMUM_KEYS)
            .expireAfterAccess(IDLE_EXPIRY)
            .ticker(ticker)
            .build();
    }

    /**
     * 이번 차단을 로그로 남길 차례면 직전 로그 이후 생략한 차단 수를 돌려주고, 생략할 차례면 비어 있다.
     */
    public OptionalLong tryAcquire(String limiterKey) {
        long now = ticker.read();
        long[] suppressed = {-1L};
        windows.asMap().compute(limiterKey, (key, window) -> {
            if (window == null || now - window.startedAtNanos() >= LOG_INTERVAL.toNanos()) {
                suppressed[0] = window == null ? 0L : window.suppressed();
                return new Window(now, 0L);
            }
            return new Window(window.startedAtNanos(), window.suppressed() + 1);
        });
        return suppressed[0] < 0 ? OptionalLong.empty() : OptionalLong.of(suppressed[0]);
    }

    private record Window(long startedAtNanos, long suppressed) {
    }
}
