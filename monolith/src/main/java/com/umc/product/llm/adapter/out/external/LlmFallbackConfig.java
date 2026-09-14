package com.umc.product.llm.adapter.out.external;

import com.umc.product.llm.application.port.out.ChatCompletionPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 어떤 활성 LLM 어댑터도 컨테이너에 등록되지 못한 경우의 안전 fallback.
 * <p>
 * 다음과 같은 운영 사고 시 부팅이 실패하지 않도록 mock 어댑터를 fallback 으로 등록한다.
 * <ul>
 *   <li>{@code LLM_PROVIDER} 가 우리가 정의한 값({@code mock | openai | vertexai-gemini | google-genai}) 외의 값
 *       (오타 또는 legacy 값) 으로 주입되어 모든 어댑터의 {@code @ConditionalOnProperty} 가 매칭 실패</li>
 *   <li>활성 provider 의 ChatModel 자동구성이 트리거 자체는 됐으나 인증 자격 등 부족으로 빈이 만들어지지 못해
 *       어댑터의 {@code @ConditionalOnBean} 이 false 로 평가</li>
 * </ul>
 * fallback 활성화 시 WARN 로그가 출력되므로 운영자가 즉시 인지할 수 있다.
 */
@Slf4j
@Configuration
public class LlmFallbackConfig {

    @Bean(name = "fallbackChatCompletionPort")
    @ConditionalOnMissingBean(ChatCompletionPort.class)
    ChatCompletionPort fallbackChatCompletionPort(LlmProperties properties) {
        log.warn(
            "활성화된 LLM 어댑터가 없어 mock 어댑터로 fallback 합니다. "
                + "현재 app.llm.provider={} 입니다. 후보값: mock | openai | vertexai-gemini | google-genai. "
                + "운영 환경에서는 LLM_PROVIDER 환경 변수와 활성 provider 의 인증 환경 변수를 함께 점검하세요.",
            properties.provider()
        );
        return new MockChatCompletionAdapter();
    }
}
