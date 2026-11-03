package com.umc.product.global.ratelimit;

import java.time.Duration;
import java.util.OptionalLong;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;

/**
 * 차단 로그를 limiter key마다 한 구간에 한 줄로 줄인다.
 *
 * <p>구간 안의 나머지 차단은 개수만 세고, 다음 구간의 첫 줄에 그 개수를 함께 남긴다.
 * 정확한 차단 총량은 {@link ApiRateLimitMetrics}의 blocked counter가 맡는다.
 *
 * <p>key 항목 수 상한과 만료는 bucket cache와 같은 {@link ApiRateLimitProperties.Cache} 값을 쓴다.
 * sampler가 bucket cache보다 작으면 bucket이 남은 key의 구간만 먼저 밀려나 차단마다 로그가 다시 남기 때문이다.
 * 만료되거나 밀려난 key의 남은 개수는 로그에 남지 않는다.
 */
public class RateLimitBlockLogSampler {

    private static final Duration LOG_INTERVAL = Duration.ofMinutes(1);

    private final Ticker ticker;
    private final Cache<String, Window> windows;

    public RateLimitBlockLogSampler(ApiRateLimitProperties.Cache cache, Ticker ticker) {
        this.ticker = ticker;
        this.windows = Caffeine.newBuilder()
            .maximumSize(cache.maximumSize())
            .expireAfterAccess(cache.expireAfterAccess())
            .ticker(ticker)
            // 상한 정리를 차단한 요청 스레드에서 바로 해서 key 수가 상한을 넘은 채로 남지 않게 한다.
            .executor(Runnable::run)
            .build();
    }

    /**
     * 이번 차단을 로그로 남길 차례면 직전 로그 이후 생략한 차단 수를 돌려주고, 생략할 차례면 비어 있다.
     */
    public OptionalLong tryAcquire(String limiterKey) {
        long now = ticker.read();
        return windows.asMap()
            .compute(limiterKey, (key, window) -> window == null ? Window.open(now, 0L) : window.next(now))
            .logLine();
    }

    private record Window(long startedAtNanos, long suppressed, OptionalLong logLine) {

        static Window open(long now, long suppressedBefore) {
            return new Window(now, 0L, OptionalLong.of(suppressedBefore));
        }

        Window next(long now) {
            if (now - startedAtNanos >= LOG_INTERVAL.toNanos()) {
                return open(now, suppressed);
            }
            return new Window(startedAtNanos, suppressed + 1, OptionalLong.empty());
        }
    }
}
