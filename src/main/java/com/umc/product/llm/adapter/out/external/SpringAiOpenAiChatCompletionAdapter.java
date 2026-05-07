package com.umc.product.llm.adapter.out.external;

import com.umc.product.llm.application.port.in.dto.ChatCompleteCommand;
import com.umc.product.llm.application.port.in.dto.ChatCompletionResult;
import com.umc.product.llm.application.port.out.ChatCompletionPort;
import com.umc.product.llm.domain.exception.LlmDomainException;
import com.umc.product.llm.domain.exception.LlmErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Spring AI ChatClient 위에 얹은 OpenAI 어댑터.
 * <p>
 * application property 의 {@code app.llm.provider=openai} 일 때만 활성화된다.
 * 모델/온도/max-tokens 는 {@link LlmProperties} 가 결정하고, 분류 호출에서는
 * {@link ChatPromptHelper} 가 system prompt 에 후보 제약을 강제한다.
 * <p>
 * 응답이 후보 외 값인지 여부는 호출자(예: FigmaCommentDomainClassifier) 가 판정한다.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.llm.provider", havingValue = "openai")
@ConditionalOnBean(OpenAiChatModel.class)
public class SpringAiOpenAiChatCompletionAdapter implements ChatCompletionPort {

    private static final String PROVIDER_NAME = "openai";

    private final OpenAiChatModel chatModel;
    private final LlmProperties properties;

    public SpringAiOpenAiChatCompletionAdapter(OpenAiChatModel chatModel, LlmProperties properties) {
        this.chatModel = chatModel;
        this.properties = properties;
    }

    @Override
    public ChatCompletionResult complete(ChatCompleteCommand command) {
        OpenAiChatOptions options = OpenAiChatOptions.builder()
            .model(properties.model())
            .temperature(properties.temperature())
            .maxTokens(properties.maxOutputTokens())
            .build();

        String systemPrompt = ChatPromptHelper.buildSystemPrompt(command);
        String userPrompt = command.userPrompt() == null ? "" : command.userPrompt();

        try {
            String content = ChatClient.builder(chatModel)
                .build()
                .prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .options(options)
                .call()
                .content();
            String normalized = ChatPromptHelper.normalizeResponse(content);
            log.debug("OpenAI 호출 성공: model={}, classification={}, length={}",
                properties.model(), command.isClassification(), normalized.length());
            return ChatCompletionResult.of(normalized, PROVIDER_NAME);
        } catch (Exception e) {
            log.warn("OpenAI 호출 실패: model={}, error={}", properties.model(), e.toString());
            throw new LlmDomainException(LlmErrorCode.CHAT_COMPLETION_FAILED, e.getMessage());
        }
    }

    @Override
    public String providerName() {
        return PROVIDER_NAME;
    }
}
