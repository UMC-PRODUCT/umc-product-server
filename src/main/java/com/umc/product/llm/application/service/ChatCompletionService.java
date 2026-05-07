package com.umc.product.llm.application.service;

import com.umc.product.llm.application.port.in.ChatCompleteUseCase;
import com.umc.product.llm.application.port.in.dto.ChatCompleteCommand;
import com.umc.product.llm.application.port.in.dto.ChatCompletionResult;
import com.umc.product.llm.application.port.out.ChatCompletionPort;
import com.umc.product.llm.domain.exception.LlmDomainException;
import com.umc.product.llm.domain.exception.LlmErrorCode;
import jakarta.annotation.PostConstruct;
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
 * 호출 전후로 {@link LlmCallGuard} (회로 차단), {@link LlmRateLimiter} (사전 페이싱),
 * {@link LlmMetrics} (관측) 를 적용한다. 일시적 실패의 retry 는 Spring AI 자동구성에
 * 위임하고, 본 서비스는 retry 가 모두 소진된 뒤의 최종 결과만 가드/메트릭에 반영한다.
 */
@Slf4j
@Service
public class ChatCompletionService implements ChatCompleteUseCase {

    private final ChatCompletionPort chatCompletionPort;
    private final LlmCallGuard callGuard;
    private final LlmRateLimiter rateLimiter;
    private final LlmMetrics metrics;
    private final Clock clock;

    @Autowired
    public ChatCompletionService(
        ChatCompletionPort chatCompletionPort,
        LlmCallGuard callGuard,
        LlmRateLimiter rateLimiter,
        LlmMetrics metrics
    ) {
        this(chatCompletionPort, callGuard, rateLimiter, metrics, Clock.systemUTC());
    }

    ChatCompletionService(
        ChatCompletionPort chatCompletionPort,
        LlmCallGuard callGuard,
        LlmRateLimiter rateLimiter,
        LlmMetrics metrics,
        Clock clock
    ) {
        this.chatCompletionPort = chatCompletionPort;
        this.callGuard = callGuard;
        this.rateLimiter = rateLimiter;
        this.metrics = metrics;
        this.clock = clock;
    }

    /**
     * 부팅 시 실제로 활성화된 어댑터를 INFO 로 한 줄 출력해 운영자가 LLM_PROVIDER 설정과
     * 실제 활성 provider 의 일치 여부를 즉시 확인할 수 있게 한다.
     */
    @PostConstruct
    void logActiveProvider() {
        log.info("LLM 활성 provider={} (어댑터={})",
            chatCompletionPort.providerName(),
            chatCompletionPort.getClass().getSimpleName());
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
        rateLimiter.acquire();

        int systemPromptLen = command.systemPrompt() == null ? 0 : command.systemPrompt().length();
        int userPromptLen = command.userPrompt() == null ? 0 : command.userPrompt().length();
        log.info("LLM 호출 시작: provider={}, systemPromptLen={}, userPromptLen={}, maxOutputTokensOverride={}",
            provider, systemPromptLen, userPromptLen, command.maxOutputTokensOverride());

        Instant start = clock.instant();
        try {
            ChatCompletionResult result = chatCompletionPort.complete(command);
            Duration latency = Duration.between(start, clock.instant());
            metrics.recordCall(result.provider(), LlmMetrics.STATUS_SUCCESS, latency);
            metrics.recordTokens(result.provider(), result.promptTokens(), result.completionTokens());
            callGuard.recordSuccess();
            log.info("LLM 호출 완료: provider={}, latencyMs={}, promptTokens={}, completionTokens={}, responseLen={}",
                result.provider(),
                latency.toMillis(),
                result.promptTokens(),
                result.completionTokens(),
                result.text() == null ? 0 : result.text().length());
            return result;
        } catch (LlmDomainException e) {
            Duration latency = Duration.between(start, clock.instant());
            metrics.recordCall(provider, LlmMetrics.STATUS_FAILED, latency);
            callGuard.recordFailure();
            log.warn("LLM 호출 실패: provider={}, latencyMs={}, baseCode={}, message={}",
                provider, latency.toMillis(), e.getBaseCode(), e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            Duration latency = Duration.between(start, clock.instant());
            metrics.recordCall(provider, LlmMetrics.STATUS_FAILED, latency);
            callGuard.recordFailure();
            log.warn("LLM 호출 중 예기치 못한 예외: provider={}, latencyMs={}, error={}",
                provider, latency.toMillis(), e.toString());
            throw new LlmDomainException(LlmErrorCode.CHAT_COMPLETION_FAILED, e.getMessage());
        }
    }
}
