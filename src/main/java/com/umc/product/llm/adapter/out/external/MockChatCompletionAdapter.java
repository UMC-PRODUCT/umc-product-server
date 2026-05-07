package com.umc.product.llm.adapter.out.external;

import com.umc.product.llm.application.port.in.dto.ChatCompleteCommand;
import com.umc.product.llm.application.port.in.dto.ChatCompletionResult;
import com.umc.product.llm.application.port.out.ChatCompletionPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 실제 LLM 호출을 대체하는 mock 어댑터.
 * <p>
 * userPrompt 의 앞 일부를 그대로 echo 한다. 어떤 사용처 의미도 알지 않으므로 분류 같은 호출에서는
 * 결과가 후보에 매칭되지 않아 호출자(예: figma classifier) 가 fallback 채널로 흡수한다.
 * 운영자는 mock 모드로 figma flow 의 fallback path 만 검증하고, 분류 정확도는 실 provider 활성화
 * 후 검증한다.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.llm.provider", havingValue = "mock", matchIfMissing = true)
public class MockChatCompletionAdapter implements ChatCompletionPort {

    private static final String PROVIDER_NAME = "mock";
    private static final int ECHO_PREVIEW_LIMIT = 80;

    @Override
    public ChatCompletionResult complete(ChatCompleteCommand command) {
        String prompt = command.userPrompt() == null ? "" : command.userPrompt();
        String echoed = prompt.length() <= ECHO_PREVIEW_LIMIT
            ? prompt
            : prompt.substring(0, ECHO_PREVIEW_LIMIT) + "...";
        log.debug("Mock LLM echo: length={}", prompt.length());
        return ChatCompletionResult.of("[mock] " + echoed, PROVIDER_NAME);
    }

    @Override
    public String providerName() {
        return PROVIDER_NAME;
    }
}
