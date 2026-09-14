package com.umc.product.llm.adapter.out.external;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.umc.product.llm.application.port.in.dto.ChatCompleteCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;

@DisplayName("ChatPromptHelper")
class ChatPromptHelperTest {

    @Test
    @DisplayName("응답 정규화는 앞뒤 공백을 제거하고 null 은 빈 문자열로 처리한다")
    void 응답_정규화() {
        assertThat(ChatPromptHelper.normalizeResponse("  hello  ")).isEqualTo("hello");
        assertThat(ChatPromptHelper.normalizeResponse("\nx\n")).isEqualTo("x");
        assertThat(ChatPromptHelper.normalizeResponse(null)).isEmpty();
    }

    @Test
    @DisplayName("ChatResponse 메타데이터에서 토큰 사용량을 long 으로 추출한다")
    void 토큰_추출_성공() {
        Usage usage = mock(Usage.class);
        when(usage.getPromptTokens()).thenReturn(120);
        when(usage.getCompletionTokens()).thenReturn(8);
        ChatResponseMetadata metadata = mock(ChatResponseMetadata.class);
        when(metadata.getUsage()).thenReturn(usage);
        ChatResponse response = mock(ChatResponse.class);
        when(response.getMetadata()).thenReturn(metadata);

        assertThat(ChatPromptHelper.extractPromptTokens(response)).isEqualTo(120L);
        assertThat(ChatPromptHelper.extractCompletionTokens(response)).isEqualTo(8L);
    }

    @Test
    @DisplayName("ChatResponse 또는 메타데이터가 부재하면 토큰 추출은 null 을 반환한다")
    void 토큰_추출_부재() {
        assertThat(ChatPromptHelper.extractPromptTokens(null)).isNull();
        assertThat(ChatPromptHelper.extractCompletionTokens(null)).isNull();

        ChatResponse response = mock(ChatResponse.class);
        when(response.getMetadata()).thenReturn(null);
        assertThat(ChatPromptHelper.extractPromptTokens(response)).isNull();
    }

    @Test
    @DisplayName("max-tokens override 가 있으면 그 값을, 없으면 properties 기본값을 반환한다")
    void max_tokens_해석() {
        LlmProperties properties = new LlmProperties("mock", "model", 0.0, 32, null, null, null);

        ChatCompleteCommand withOverride = ChatCompleteCommand.freeFormWithMaxTokens("s", "u", 256);
        ChatCompleteCommand withoutOverride = ChatCompleteCommand.freeForm("s", "u");

        assertThat(ChatPromptHelper.resolveMaxOutputTokens(withOverride, properties)).isEqualTo(256);
        assertThat(ChatPromptHelper.resolveMaxOutputTokens(withoutOverride, properties)).isEqualTo(32);
    }
}
