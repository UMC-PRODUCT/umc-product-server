package com.umc.product.global.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.OptionalLong;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RateLimitBlockLogSamplerTest {

    private final AtomicLong nanos = new AtomicLong();
    private final RateLimitBlockLogSampler sampler = new RateLimitBlockLogSampler(nanos::get);

    @Test
    @DisplayName("1분 안의 반복 차단은 생략하고 다음 구간 첫 로그에 생략한 개수를 남긴다")
    void reports_suppressed_count_in_next_window() {
        // given
        assertThat(sampler.tryAcquire("ip:10.0.0.1:GET:/api/v1/projects")).hasValue(0L);

        // when
        for (int block = 0; block < 3; block++) {
            assertThat(sampler.tryAcquire("ip:10.0.0.1:GET:/api/v1/projects")).isEmpty();
        }
        nanos.addAndGet(Duration.ofMinutes(1).toNanos());

        // then
        assertThat(sampler.tryAcquire("ip:10.0.0.1:GET:/api/v1/projects")).hasValue(3L);
        assertThat(sampler.tryAcquire("ip:10.0.0.1:GET:/api/v1/projects")).isEmpty();
    }

    @Test
    @DisplayName("limiter key가 다르면 같은 구간에서도 각자 첫 차단을 로그로 남긴다")
    void each_key_logs_its_first_block() {
        // given
        sampler.tryAcquire("ip:10.0.0.1:GET:/api/v1/projects");

        // when
        OptionalLong otherKey = sampler.tryAcquire("ip:10.0.0.2:GET:/api/v1/projects");

        // then
        assertThat(otherKey).hasValue(0L);
    }
}
