package com.umc.product.llm.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.umc.product.llm.adapter.out.external.LlmProperties;
import java.time.Clock;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("LlmCallGuard")
@ExtendWith(MockitoExtension.class)
class LlmCallGuardTest {

    private static final Instant T0 = Instant.parse("2026-05-07T00:00:00Z");

    @Mock
    private Clock clock;

    private LlmCallGuard guard;

    @BeforeEach
    void setUp() {
        LlmProperties properties = new LlmProperties(
            "mock", "model", 0.0, 32, null,
            new LlmProperties.CircuitBreaker(3, 60_000L)
        );
        when(clock.instant()).thenReturn(T0);
        guard = new LlmCallGuard(properties, clock);
    }

    @Test
    @DisplayName("초기 상태는 호출을 허용한다")
    void 초기_상태_허용() {
        assertThat(guard.allow()).isTrue();
    }

    @Test
    @DisplayName("연속 실패가 임계 미만이면 차단되지 않는다")
    void 임계_미만_허용() {
        guard.recordFailure();
        guard.recordFailure();

        assertThat(guard.allow()).isTrue();
    }

    @Test
    @DisplayName("연속 실패가 임계 도달하면 openDuration 동안 차단된다")
    void 임계_도달_차단() {
        guard.recordFailure();
        guard.recordFailure();
        guard.recordFailure();

        when(clock.instant()).thenReturn(T0.plusMillis(30_000L));
        assertThat(guard.allow()).isFalse();

        when(clock.instant()).thenReturn(T0.plusMillis(60_001L));
        assertThat(guard.allow()).isTrue();
    }

    @Test
    @DisplayName("성공 호출은 카운터와 차단 상태를 모두 초기화한다")
    void 성공_시_상태_초기화() {
        guard.recordFailure();
        guard.recordFailure();
        guard.recordSuccess();

        guard.recordFailure();
        guard.recordFailure();

        assertThat(guard.allow()).isTrue();
    }
}
