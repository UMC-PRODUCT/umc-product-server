package com.umc.product.llm.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.umc.product.llm.application.port.in.dto.ChatCompleteCommand;
import com.umc.product.llm.application.port.in.dto.ChatCompletionResult;
import com.umc.product.llm.application.port.out.ChatCompletionPort;
import com.umc.product.llm.domain.exception.LlmDomainException;
import com.umc.product.llm.domain.exception.LlmErrorCode;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("ChatCompletionService")
@ExtendWith(MockitoExtension.class)
class ChatCompletionServiceTest {

    private static final Instant T0 = Instant.parse("2026-05-07T00:00:00Z");
    private static final String PROVIDER = "openai";

    @Mock
    private ChatCompletionPort port;

    @Mock
    private LlmCallGuard guard;

    @Mock
    private Clock clock;

    private MeterRegistry registry;
    private LlmMetrics metrics;
    private ChatCompletionService service;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        metrics = new LlmMetrics(registry);
        lenient().when(clock.instant()).thenReturn(T0, T0.plusMillis(120));
        when(port.providerName()).thenReturn(PROVIDER);
        service = new ChatCompletionService(port, guard, metrics, clock);
    }

    @Test
    @DisplayName("가드가 차단 상태이면 즉시 실패하고 circuit-open 메트릭을 기록한다")
    void 가드_차단_즉시_실패() {
        when(guard.allow()).thenReturn(false);

        assertThatThrownBy(() -> service.complete(ChatCompleteCommand.freeForm("s", "u")))
            .isInstanceOf(LlmDomainException.class);

        Counter counter = registry.find("llm.chat.completion.total")
            .tag("provider", PROVIDER)
            .tag("status", LlmMetrics.STATUS_CIRCUIT_OPEN)
            .counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
        verify(port, never()).complete(any());
    }

    @Test
    @DisplayName("정상 응답이고 분류 후보 안에 있으면 success 메트릭과 토큰 카운터를 증가시킨다")
    void 분류_정상_응답() {
        when(guard.allow()).thenReturn(true);
        when(port.complete(any())).thenReturn(
            ChatCompletionResult.of("a", PROVIDER, 100L, 5L)
        );

        ChatCompleteCommand command = ChatCompleteCommand.classify("s", "u", List.of("a", "b"));
        ChatCompletionResult result = service.complete(command);

        assertThat(result.text()).isEqualTo("a");
        Timer timer = registry.find("llm.chat.completion.seconds")
            .tag("provider", PROVIDER)
            .tag("status", LlmMetrics.STATUS_SUCCESS)
            .timer();
        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(1L);
        assertThat(registry.find("llm.chat.completion.tokens.total")
            .tag("provider", PROVIDER).tag("type", "in").counter().count()).isEqualTo(100.0);
        assertThat(registry.find("llm.chat.completion.tokens.total")
            .tag("provider", PROVIDER).tag("type", "out").counter().count()).isEqualTo(5.0);
        verify(guard, times(1)).recordSuccess();
    }

    @Test
    @DisplayName("분류 응답이 후보 외 값이면 out-of-candidates 메트릭으로 기록한다")
    void 분류_후보_외_응답() {
        when(guard.allow()).thenReturn(true);
        when(port.complete(any())).thenReturn(
            ChatCompletionResult.of("zzz", PROVIDER, 50L, 3L)
        );

        ChatCompleteCommand command = ChatCompleteCommand.classify("s", "u", List.of("a", "b"));
        service.complete(command);

        Counter counter = registry.find("llm.chat.completion.total")
            .tag("provider", PROVIDER)
            .tag("status", LlmMetrics.STATUS_OUT_OF_CANDIDATES)
            .counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
        verify(guard, times(1)).recordSuccess();
    }

    @Test
    @DisplayName("어댑터가 LlmDomainException 을 던지면 failed 메트릭을 기록하고 가드 실패를 누적한다")
    void 어댑터_실패() {
        when(guard.allow()).thenReturn(true);
        when(port.complete(any())).thenThrow(new LlmDomainException(LlmErrorCode.CHAT_COMPLETION_FAILED));

        assertThatThrownBy(() -> service.complete(ChatCompleteCommand.freeForm("s", "u")))
            .isInstanceOf(LlmDomainException.class);

        Counter counter = registry.find("llm.chat.completion.total")
            .tag("provider", PROVIDER)
            .tag("status", LlmMetrics.STATUS_FAILED)
            .counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
        verify(guard, times(1)).recordFailure();
    }
}
