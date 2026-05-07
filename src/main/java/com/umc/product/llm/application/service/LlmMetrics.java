package com.umc.product.llm.application.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.Duration;
import org.springframework.stereotype.Component;

/**
 * LLM 호출 관측 메트릭 등록자.
 * <p>
 * cardinality 폭증을 막기 위해 라벨은 provider, status, type 으로만 제한한다 (ADR-006).
 *
 * <ul>
 *   <li>{@code llm_chat_completion_seconds{provider, status}} – 호출 latency</li>
 *   <li>{@code llm_chat_completion_total{provider, status}} – 호출 카운트</li>
 *   <li>{@code llm_chat_completion_tokens_total{provider, type}} – 토큰 사용량 (type=in|out)</li>
 * </ul>
 */
@Component
public class LlmMetrics {

    public static final String STATUS_SUCCESS = "success";
    public static final String STATUS_OUT_OF_CANDIDATES = "out-of-candidates";
    public static final String STATUS_FAILED = "failed";
    public static final String STATUS_CIRCUIT_OPEN = "circuit-open";

    private static final String METRIC_LATENCY = "llm.chat.completion.seconds";
    private static final String METRIC_TOTAL = "llm.chat.completion.total";
    private static final String METRIC_TOKENS = "llm.chat.completion.tokens.total";

    private static final String TAG_PROVIDER = "provider";
    private static final String TAG_STATUS = "status";
    private static final String TAG_TYPE = "type";
    private static final String TYPE_IN = "in";
    private static final String TYPE_OUT = "out";

    private final MeterRegistry registry;

    public LlmMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void recordCall(String provider, String status, Duration latency) {
        Timer.builder(METRIC_LATENCY)
            .tag(TAG_PROVIDER, provider)
            .tag(TAG_STATUS, status)
            .register(registry)
            .record(latency);
        Counter.builder(METRIC_TOTAL)
            .tag(TAG_PROVIDER, provider)
            .tag(TAG_STATUS, status)
            .register(registry)
            .increment();
    }

    public void recordTokens(String provider, Long promptTokens, Long completionTokens) {
        if (promptTokens != null && promptTokens > 0) {
            Counter.builder(METRIC_TOKENS)
                .tag(TAG_PROVIDER, provider)
                .tag(TAG_TYPE, TYPE_IN)
                .register(registry)
                .increment(promptTokens);
        }
        if (completionTokens != null && completionTokens > 0) {
            Counter.builder(METRIC_TOKENS)
                .tag(TAG_PROVIDER, provider)
                .tag(TAG_TYPE, TYPE_OUT)
                .register(registry)
                .increment(completionTokens);
        }
    }
}
