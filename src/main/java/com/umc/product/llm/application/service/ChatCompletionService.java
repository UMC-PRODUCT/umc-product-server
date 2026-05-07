package com.umc.product.llm.application.service;

import com.umc.product.llm.application.port.in.ChatCompleteUseCase;
import com.umc.product.llm.application.port.in.dto.ChatCompleteCommand;
import com.umc.product.llm.application.port.in.dto.ChatCompletionResult;
import com.umc.product.llm.application.port.out.ChatCompletionPort;
import com.umc.product.llm.domain.exception.LlmDomainException;
import com.umc.product.llm.domain.exception.LlmErrorCode;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * LLM 호출 진입점. 활성화된 단일 ChatCompletionPort 구현체에 위임한다.
 * provider 교체 (mock → openai → gemini → spring-ai) 는 어댑터 레벨에서만 일어난다.
 * <p>
 * 호출 전후로 {@link LlmCallGuard} 를 통해 회로 차단 상태를 확인하고 결과를 기록하며,
 * {@link LlmMetrics} 로 latency / status / 토큰 사용량을 관측한다.
 * 일시적 실패의 retry 는 Spring AI 자동구성에 위임하고, 본 서비스는 retry 가 모두 소진된
 * 뒤의 최종 결과만 가드/메트릭에 반영한다.
 */
@Slf4j
@Service
public class ChatCompletionService implements ChatCompleteUseCase {

    private final ChatCompletionPort chatCompletionPort;
    private final LlmCallGuard callGuard;
    private final LlmMetrics metrics;
    private final Clock clock;

    @Autowired
    public ChatCompletionService(
        ChatCompletionPort chatCompletionPort,
        LlmCallGuard callGuard,
        LlmMetrics metrics
    ) {
        this(chatCompletionPort, callGuard, metrics, Clock.systemUTC());
    }

    ChatCompletionService(
        ChatCompletionPort chatCompletionPort,
        LlmCallGuard callGuard,
        LlmMetrics metrics,
        Clock clock
    ) {
        this.chatCompletionPort = chatCompletionPort;
        this.callGuard = callGuard;
        this.metrics = metrics;
        this.clock = clock;
    }

    @Override
    public ChatCompletionResult complete(ChatCompleteCommand command) {
        if (chatCompletionPort == null) {
            throw new LlmDomainException(LlmErrorCode.PROVIDER_NOT_CONFIGURED);
        }
        String provider = chatCompletionPort.providerName();
        if (!callGuard.allow()) {
            log.debug("LLM 호출 가드가 차단 상태입니다. 즉시 실패 응답 반환.");
            metrics.recordCall(provider, LlmMetrics.STATUS_CIRCUIT_OPEN, Duration.ZERO);
            throw new LlmDomainException(LlmErrorCode.CHAT_COMPLETION_FAILED, "회로 차단 상태");
        }
        Instant start = clock.instant();
        try {
            ChatCompletionResult result = chatCompletionPort.complete(command);
            Duration latency = Duration.between(start, clock.instant());
            String status = resolveSuccessStatus(command, result);
            metrics.recordCall(result.provider(), status, latency);
            metrics.recordTokens(result.provider(), result.promptTokens(), result.completionTokens());
            callGuard.recordSuccess();
            return result;
        } catch (LlmDomainException e) {
            metrics.recordCall(provider, LlmMetrics.STATUS_FAILED, Duration.between(start, clock.instant()));
            callGuard.recordFailure();
            throw e;
        } catch (RuntimeException e) {
            metrics.recordCall(provider, LlmMetrics.STATUS_FAILED, Duration.between(start, clock.instant()));
            callGuard.recordFailure();
            log.warn("LLM 호출 중 예기치 못한 예외: {}", e.toString());
            throw new LlmDomainException(LlmErrorCode.CHAT_COMPLETION_FAILED, e.getMessage());
        }
    }

    private String resolveSuccessStatus(ChatCompleteCommand command, ChatCompletionResult result) {
        if (!command.isClassification()) {
            return LlmMetrics.STATUS_SUCCESS;
        }
        String text = result.text();
        if (text == null || !command.candidates().contains(text)) {
            return LlmMetrics.STATUS_OUT_OF_CANDIDATES;
        }
        return LlmMetrics.STATUS_SUCCESS;
    }
}
