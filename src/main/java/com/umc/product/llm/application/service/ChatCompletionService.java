package com.umc.product.llm.application.service;

import com.umc.product.llm.application.port.in.ChatCompleteUseCase;
import com.umc.product.llm.application.port.in.dto.ChatCompleteCommand;
import com.umc.product.llm.application.port.in.dto.ChatCompletionResult;
import com.umc.product.llm.application.port.out.ChatCompletionPort;
import com.umc.product.llm.domain.exception.LlmDomainException;
import com.umc.product.llm.domain.exception.LlmErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * LLM 호출 진입점. 활성화된 단일 ChatCompletionPort 구현체에 위임한다.
 * provider 교체 (mock → openai → gemini → spring-ai) 는 어댑터 레벨에서만 일어난다.
 * <p>
 * 호출 전후로 {@link LlmCallGuard} 를 통해 회로 차단 상태를 확인하고 결과를 기록한다.
 * 일시적 실패의 retry 는 Spring AI 자동구성에 위임하며, 본 서비스는 retry 가
 * 모두 소진된 뒤의 최종 결과만 가드에 반영한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatCompletionService implements ChatCompleteUseCase {

    private final ChatCompletionPort chatCompletionPort;
    private final LlmCallGuard callGuard;

    @Override
    public ChatCompletionResult complete(ChatCompleteCommand command) {
        if (chatCompletionPort == null) {
            throw new LlmDomainException(LlmErrorCode.PROVIDER_NOT_CONFIGURED);
        }
        if (!callGuard.allow()) {
            log.debug("LLM 호출 가드가 차단 상태입니다. 즉시 실패 응답 반환.");
            throw new LlmDomainException(LlmErrorCode.CHAT_COMPLETION_FAILED, "회로 차단 상태");
        }
        try {
            ChatCompletionResult result = chatCompletionPort.complete(command);
            callGuard.recordSuccess();
            return result;
        } catch (LlmDomainException e) {
            callGuard.recordFailure();
            throw e;
        } catch (RuntimeException e) {
            callGuard.recordFailure();
            log.warn("LLM 호출 중 예기치 못한 예외: {}", e.toString());
            throw new LlmDomainException(LlmErrorCode.CHAT_COMPLETION_FAILED, e.getMessage());
        }
    }
}
