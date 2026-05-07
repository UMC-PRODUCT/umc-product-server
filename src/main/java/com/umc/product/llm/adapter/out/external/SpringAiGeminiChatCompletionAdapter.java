package com.umc.product.llm.adapter.out.external;

import com.umc.product.llm.application.port.in.dto.ChatCompleteCommand;
import com.umc.product.llm.application.port.in.dto.ChatCompletionResult;
import com.umc.product.llm.application.port.out.ChatCompletionPort;
import com.umc.product.llm.domain.exception.LlmDomainException;
import com.umc.product.llm.domain.exception.LlmErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Spring AI ChatClient 위에 얹은 Vertex AI Gemini 어댑터.
 * <p>
 * application property 의 {@code app.llm.provider=gemini} 일 때만 활성화된다.
 * 응답 파싱은 OpenAI 어댑터와 동일하게 {@link ChatPromptHelper} 를 재사용한다.
 * <p>
 * 인증 자격은 {@code GOOGLE_APPLICATION_CREDENTIALS} 환경 변수의 service account 또는
 * Spring AI 의 Vertex 자동구성이 인식하는 다른 자격 경로로 주입된다.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.llm.provider", havingValue = "gemini")
@ConditionalOnBean(VertexAiGeminiChatModel.class)
public class SpringAiGeminiChatCompletionAdapter implements ChatCompletionPort {

    private static final String PROVIDER_NAME = "gemini";

    private final VertexAiGeminiChatModel chatModel;
    private final LlmProperties properties;

    public SpringAiGeminiChatCompletionAdapter(VertexAiGeminiChatModel chatModel, LlmProperties properties) {
        this.chatModel = chatModel;
        this.properties = properties;
    }

    @Override
    public ChatCompletionResult complete(ChatCompleteCommand command) {
        VertexAiGeminiChatOptions options = VertexAiGeminiChatOptions.builder()
            .model(properties.model())
            .temperature(properties.temperature())
            .maxOutputTokens(properties.maxOutputTokens())
            .build();

        String systemPrompt = ChatPromptHelper.buildSystemPrompt(command);
        String userPrompt = command.userPrompt() == null ? "" : command.userPrompt();

        try {
            ChatResponse response = ChatClient.builder(chatModel)
                .build()
                .prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .options(options)
                .call()
                .chatResponse();
            String content = response == null || response.getResult() == null || response.getResult().getOutput() == null
                ? ""
                : response.getResult().getOutput().getText();
            String normalized = ChatPromptHelper.normalizeResponse(content);
            Long promptTokens = ChatPromptHelper.extractPromptTokens(response);
            Long completionTokens = ChatPromptHelper.extractCompletionTokens(response);
            log.debug("Gemini 호출 성공: model={}, classification={}, length={}, promptTokens={}, completionTokens={}",
                properties.model(), command.isClassification(), normalized.length(), promptTokens, completionTokens);
            return ChatCompletionResult.of(normalized, PROVIDER_NAME, promptTokens, completionTokens);
        } catch (Exception e) {
            log.warn("Gemini 호출 실패: model={}, error={}", properties.model(), e.toString());
            throw new LlmDomainException(LlmErrorCode.CHAT_COMPLETION_FAILED, e.getMessage());
        }
    }

    @Override
    public String providerName() {
        return PROVIDER_NAME;
    }
}
