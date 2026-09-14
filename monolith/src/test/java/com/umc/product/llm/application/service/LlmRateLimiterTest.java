package com.umc.product.llm.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.umc.product.llm.adapter.out.external.LlmProperties;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("LlmRateLimiter")
class LlmRateLimiterTest {

    private static final Instant T0 = Instant.parse("2026-05-07T00:00:00Z");

    @Test
    @DisplayName("requestsPerMinute=0 이면 acquire 가 즉시 통과한다")
    void 페이싱_비활성_즉시_통과() {
        LlmProperties properties = propertiesWithRateLimit(0, 5);
        LlmRateLimiter limiter = new LlmRateLimiter(properties, fixedClock(T0));

        assertThatCode(limiter::acquire).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("burst 개수만큼은 sleep 없이 즉시 acquire 한다")
    void burst_허용_범위_즉시_통과() {
        LlmProperties properties = propertiesWithRateLimit(60, 3);
        AtomicReference<Instant> nowRef = new AtomicReference<>(T0);
        LlmRateLimiter limiter = new LlmRateLimiter(properties, mutableClock(nowRef));

        long start = System.nanoTime();
        for (int i = 0; i < 3; i++) {
            limiter.acquire();
        }
        long elapsedMs = (System.nanoTime() - start) / 1_000_000L;

        assertThat(elapsedMs).isLessThan(50);
    }

    @Test
    @DisplayName("burst 소진 후 토큰 리필 시간이 지나면 다음 acquire 가 통과한다")
    void burst_소진_후_리필_시간_경과_시_통과() {
        LlmProperties properties = propertiesWithRateLimit(60, 1); // 1초 당 1토큰 리필
        AtomicReference<Instant> nowRef = new AtomicReference<>(T0);
        LlmRateLimiter limiter = new LlmRateLimiter(properties, mutableClock(nowRef));

        limiter.acquire();
        nowRef.set(T0.plusMillis(1_500));

        assertThatCode(limiter::acquire).doesNotThrowAnyException();
    }

    private static LlmProperties propertiesWithRateLimit(int rpm, int burst) {
        return new LlmProperties(
            "mock", "model", 0.0, 32, null, null,
            new LlmProperties.RateLimit(rpm, burst)
        );
    }

    private static Clock fixedClock(Instant instant) {
        return Clock.fixed(instant, ZoneId.of("UTC"));
    }

    private static Clock mutableClock(AtomicReference<Instant> nowRef) {
        return new Clock() {
            @Override
            public ZoneId getZone() {
                return ZoneId.of("UTC");
            }

            @Override
            public Clock withZone(ZoneId zone) {
                return this;
            }

            @Override
            public Instant instant() {
                return nowRef.get();
            }

            @Override
            public long millis() {
                return nowRef.get().toEpochMilli();
            }
        };
    }
}
