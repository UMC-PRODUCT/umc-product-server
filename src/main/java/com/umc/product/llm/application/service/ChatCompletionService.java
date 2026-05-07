package com.umc.product.llm.application.service;

import com.umc.product.llm.application.port.in.ChatCompleteUseCase;
import com.umc.product.llm.application.port.in.dto.ChatCompleteCommand;
import com.umc.product.llm.application.port.in.dto.ChatCompletionResult;
import com.umc.product.llm.application.port.out.ChatCompletionPort;
import com.umc.product.llm.domain.exception.LlmDomainException;
import com.umc.product.llm.domain.exception.LlmErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * LLM 호출 진입점. 활성화된 단일 ChatCompletionPort 구현체에 위임한다.
 * provider 교체 (mock → openai → gemini → spring-ai) 는 어댑터 레벨에서만 일어난다.
 */
@Service
@RequiredArgsConstructor
public class ChatCompletionService implements ChatCompleteUseCase {

    private final ChatCompletionPort chatCompletionPort;

    @Override
    public ChatCompletionResult complete(ChatCompleteCommand command) {
        if (chatCompletionPort == null) {
            throw new LlmDomainException(LlmErrorCode.PROVIDER_NOT_CONFIGURED);
        }
        return chatCompletionPort.complete(command);
    }
}
