package com.umc.product.global.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;
import java.util.OptionalLong;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RateLimitBlockLogSamplerTest {

    private static final String KEY = "ip:10.0.0.1:GET:/api/v1/projects";
    private static final String OTHER_KEY = "ip:10.0.0.2:GET:/api/v1/projects";

    private final AtomicLong nanos = new AtomicLong();

    @Test
    @DisplayName("1분 안의 반복 차단은 생략하고 다음 구간 첫 로그에 생략한 개수를 남긴다")
    void reports_suppressed_count_in_next_window() {
        // given
        RateLimitBlockLogSampler sampler = sampler(100, Duration.ofMinutes(10));
        assertThat(sampler.tryAcquire(KEY)).hasValue(0L);

        // when
        for (int block = 0; block < 3; block++) {
            assertThat(sampler.tryAcquire(KEY)).isEmpty();
        }
        nanos.addAndGet(Duration.ofMinutes(1).toNanos());

        // then
        assertThat(sampler.tryAcquire(KEY)).hasValue(3L);
        assertThat(sampler.tryAcquire(KEY)).isEmpty();
    }

    @Test
    @DisplayName("limiter key가 다르면 같은 구간에서도 각자 첫 차단을 로그로 남긴다")
    void each_key_logs_its_first_block() {
        // given
        RateLimitBlockLogSampler sampler = sampler(100, Duration.ofMinutes(10));
        sampler.tryAcquire(KEY);

        // when
        OptionalLong otherKey = sampler.tryAcquire(OTHER_KEY);

        // then
        assertThat(otherKey).hasValue(0L);
    }

    @Test
    @DisplayName("설정한 key 상한 안에서는 생략하고, 상한을 넘으면 밀려난 key가 같은 구간에 다시 로그를 남긴다")
    void honors_configured_maximum_size() {
        // given
        RateLimitBlockLogSampler withinLimit = sampler(2, Duration.ofMinutes(10));
        RateLimitBlockLogSampler overLimit = sampler(1, Duration.ofMinutes(10));
        blockBoth(withinLimit);
        blockBoth(overLimit);

        // when
        List<OptionalLong> withinLimitSecondRound = blockBoth(withinLimit);
        List<OptionalLong> overLimitSecondRound = blockBoth(overLimit);

        // then
        assertThat(withinLimitSecondRound).allMatch(OptionalLong::isEmpty);
        assertThat(overLimitSecondRound).anyMatch(OptionalLong::isPresent);
    }

    @Test
    @DisplayName("설정한 만료 시간 동안 차단이 없던 key는 생략 개수를 잃고 새 구간으로 시작한다")
    void honors_configured_expiry() {
        // given
        RateLimitBlockLogSampler sampler = sampler(100, Duration.ofMinutes(2));
        sampler.tryAcquire(KEY);
        sampler.tryAcquire(KEY);

        // when
        nanos.addAndGet(Duration.ofMinutes(3).toNanos());

        // then
        assertThat(sampler.tryAcquire(KEY)).hasValue(0L);
    }

    private RateLimitBlockLogSampler sampler(long maximumSize, Duration expireAfterAccess) {
        return new RateLimitBlockLogSampler(new ApiRateLimitProperties.Cache(maximumSize, expireAfterAccess), nanos::get);
    }

    private static List<OptionalLong> blockBoth(RateLimitBlockLogSampler sampler) {
        return List.of(sampler.tryAcquire(KEY), sampler.tryAcquire(OTHER_KEY));
    }
}
