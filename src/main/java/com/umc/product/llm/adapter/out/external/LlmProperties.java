package com.umc.product.llm.adapter.out.external;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * LLM provider 활성화 설정.
 * provider 값에 따라 ConditionalOnProperty 를 통해 단일 어댑터만 로드된다.
 * 후속으로 openai / gemini / spring-ai 어댑터가 추가될 때 그대로 확장된다.
 */
@ConfigurationProperties(prefix = "app.llm")
public record LlmProperties(
    String provider
) {
    public LlmProperties {
        if (provider == null || provider.isBlank()) {
            provider = "mock";
        }
    }
}
