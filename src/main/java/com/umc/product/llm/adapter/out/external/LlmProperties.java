package com.umc.product.llm.adapter.out.external;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * LLM provider 활성화 설정. provider 값에 따라 ConditionalOnProperty 를 통해 단일 어댑터만 로드된다.
 * <p>
 * model / temperature / maxOutputTokens 는 활성 어댑터가 Spring AI ChatClient 호출 시 적용할 모델 파라미터이고, retry / circuitBreaker 는 LLM
 * 도메인 차원의 호출 신뢰성 보강 설정이다.
 */
@ConfigurationProperties(prefix = "app.llm")
public record LlmProperties(
    String provider,
    String model,
    Double temperature,
    Integer maxOutputTokens,
    Retry retry,
    CircuitBreaker circuitBreaker
) {
    private static final String DEFAULT_PROVIDER = "mock";
    private static final String DEFAULT_MODEL = "gemini-2.5-flash-lite";
    private static final double DEFAULT_TEMPERATURE = 0.0;
    private static final int DEFAULT_MAX_OUTPUT_TOKENS = 32;

    public LlmProperties {
        if (provider == null || provider.isBlank()) {
            provider = DEFAULT_PROVIDER;
        }
        if (model == null || model.isBlank()) {
            model = DEFAULT_MODEL;
        }
        if (temperature == null) {
            temperature = DEFAULT_TEMPERATURE;
        }
        if (maxOutputTokens == null || maxOutputTokens <= 0) {
            maxOutputTokens = DEFAULT_MAX_OUTPUT_TOKENS;
        }
        if (retry == null) {
            retry = Retry.defaults();
        }
        if (circuitBreaker == null) {
            circuitBreaker = CircuitBreaker.defaults();
        }
    }

    public record Retry(
        int maxAttempts,
        long backoffMillis
    ) {
        private static final int DEFAULT_MAX_ATTEMPTS = 3;
        private static final long DEFAULT_BACKOFF_MILLIS = 200L;

        public static Retry defaults() {
            return new Retry(DEFAULT_MAX_ATTEMPTS, DEFAULT_BACKOFF_MILLIS);
        }
    }

    public record CircuitBreaker(
        int failureThreshold,
        long openDurationMillis
    ) {
        private static final int DEFAULT_FAILURE_THRESHOLD = 5;
        private static final long DEFAULT_OPEN_DURATION_MILLIS = 60_000L;

        public static CircuitBreaker defaults() {
            return new CircuitBreaker(DEFAULT_FAILURE_THRESHOLD, DEFAULT_OPEN_DURATION_MILLIS);
        }
    }
}
