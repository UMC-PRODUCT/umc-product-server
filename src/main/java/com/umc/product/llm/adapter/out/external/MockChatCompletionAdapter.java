package com.umc.product.llm.adapter.out.external;

import com.umc.product.llm.application.port.in.dto.ChatCompleteCommand;
import com.umc.product.llm.application.port.in.dto.ChatCompletionResult;
import com.umc.product.llm.application.port.out.ChatCompletionPort;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 실제 LLM 호출을 대체하는 mock 어댑터.
 * <p>
 * 분류 입력 (candidates 가 비어 있지 않은 경우) → 후보 중 하나를 무작위 선택해 반환한다.
 * 자유 응답 입력 → userPrompt 의 앞 일부를 그대로 echo 한다.
 * <p>
 * 추후 OpenAI / Gemini / Spring AI 어댑터가 추가되면 application property 의
 * {@code app.llm.provider} 값을 바꾸는 것만으로 활성 어댑터가 교체된다.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.llm.provider", havingValue = "mock", matchIfMissing = true)
public class MockChatCompletionAdapter implements ChatCompletionPort {

    private static final String PROVIDER_NAME = "mock";

    @Override
    public ChatCompletionResult complete(ChatCompleteCommand command) {
        if (command.isClassification()) {
            List<String> candidates = command.candidates();
            String picked = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
            log.debug("Mock LLM classification: candidates={}, picked={}", candidates, picked);
            return ChatCompletionResult.of(picked, PROVIDER_NAME);
        }

        String prompt = command.userPrompt() == null ? "" : command.userPrompt();
        String echoed = prompt.length() <= 80 ? prompt : prompt.substring(0, 80) + "...";
        return ChatCompletionResult.of("[mock] " + echoed, PROVIDER_NAME);
    }

    @Override
    public String providerName() {
        return PROVIDER_NAME;
    }
}
