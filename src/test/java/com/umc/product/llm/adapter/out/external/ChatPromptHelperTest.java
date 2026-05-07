package com.umc.product.llm.adapter.out.external;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.umc.product.llm.application.port.in.dto.ChatCompleteCommand;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;

@DisplayName("ChatPromptHelper")
class ChatPromptHelperTest {

    @Test
    @DisplayName("분류 호출이면 system prompt 끝에 후보 제약을 추가한다")
    void 분류_호출_후보_제약_추가() {
        ChatCompleteCommand command = ChatCompleteCommand.classify("기본", "본문", List.of("a", "b", "c"));

        String prompt = ChatPromptHelper.buildSystemPrompt(command);

        assertThat(prompt).contains("기본");
        assertThat(prompt).contains("a, b, c");
        assertThat(prompt).contains("정확히 하나만");
    }

    @Test
    @DisplayName("자유 응답 호출이면 system prompt 를 변형 없이 그대로 반환한다")
    void 자유_응답_호출_원본_유지() {
        ChatCompleteCommand command = ChatCompleteCommand.freeForm("기본", "본문");

        String prompt = ChatPromptHelper.buildSystemPrompt(command);

        assertThat(prompt).isEqualTo("기본");
    }

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
}
