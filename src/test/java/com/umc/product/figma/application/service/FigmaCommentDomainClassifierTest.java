package com.umc.product.figma.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.product.figma.application.port.out.dto.FigmaCommentInfo;
import com.umc.product.llm.application.port.in.ChatCompleteUseCase;
import com.umc.product.llm.application.port.in.dto.ChatCompleteCommand;
import com.umc.product.llm.application.port.in.dto.ChatCompletionResult;
import com.umc.product.llm.domain.exception.LlmDomainException;
import com.umc.product.llm.domain.exception.LlmErrorCode;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("FigmaCommentDomainClassifier")
@ExtendWith(MockitoExtension.class)
class FigmaCommentDomainClassifierTest {

    private static final List<String> CANDIDATES = List.of("auth", "challenger", "figma");

    @Mock
    private ChatCompleteUseCase chatCompleteUseCase;

    private FigmaCommentDomainClassifier classifier;

    @BeforeEach
    void setUp() {
        classifier = new FigmaCommentDomainClassifier(chatCompleteUseCase, new ObjectMapper());
    }

    @Test
    @DisplayName("동일 commentId 두 번째 호출은 LLM 을 다시 부르지 않고 캐시 결과를 반환한다")
    void 캐시_히트_시_LLM_재호출_없음() {
        FigmaCommentInfo comment = comment("c-1", "auth 관련 댓글");
        when(chatCompleteUseCase.complete(any())).thenReturn(
            ChatCompletionResult.of("auth", "openai", 100L, 5L)
        );

        String first = classifier.classify(comment, CANDIDATES);
        String second = classifier.classify(comment, CANDIDATES);

        assertThat(first).isEqualTo("auth");
        assertThat(second).isEqualTo("auth");
        verify(chatCompleteUseCase, times(1)).complete(any());
    }

    @Test
    @DisplayName("LLM 응답이 후보 외 값이면 null 을 반환하고 null 도 캐시한다")
    void 후보_외_응답_null_캐시() {
        FigmaCommentInfo comment = comment("c-2", "?");
        when(chatCompleteUseCase.complete(any())).thenReturn(
            ChatCompletionResult.of("zzz", "openai", 100L, 5L)
        );

        String first = classifier.classify(comment, CANDIDATES);
        String second = classifier.classify(comment, CANDIDATES);

        assertThat(first).isNull();
        assertThat(second).isNull();
        verify(chatCompleteUseCase, times(1)).complete(any());
    }

    @Test
    @DisplayName("LLM 호출이 실패하면 null 을 반환하고 negative 캐시한다")
    void LLM_실패_시_null_negative_캐시() {
        FigmaCommentInfo comment = comment("c-3", "msg");
        when(chatCompleteUseCase.complete(any()))
            .thenThrow(new LlmDomainException(LlmErrorCode.CHAT_COMPLETION_FAILED));

        String first = classifier.classify(comment, CANDIDATES);
        String second = classifier.classify(comment, CANDIDATES);

        assertThat(first).isNull();
        assertThat(second).isNull();
        verify(chatCompleteUseCase, times(1)).complete(any());
    }

    @Test
    @DisplayName("후보 도메인 키가 비어 있으면 LLM 을 호출하지 않고 즉시 null 을 반환한다")
    void 후보_없으면_즉시_null() {
        FigmaCommentInfo comment = comment("c-4", "msg");

        assertThat(classifier.classify(comment, List.of())).isNull();
        assertThat(classifier.classify(comment, null)).isNull();

        verifyNoInteractions(chatCompleteUseCase);
    }

    @Test
    @DisplayName("classifyBatch 는 댓글 N개를 단일 LLM 호출로 처리하고 JSON 배열 응답을 파싱한다")
    void batch_분류_단일_호출_JSON_파싱() {
        List<FigmaCommentInfo> comments = List.of(
            comment("c-1", "auth 관련"),
            comment("c-2", "challenger 관련"),
            comment("c-3", "figma 관련")
        );
        String jsonResponse = """
            [
              {"commentId":"c-1","domainKey":"auth"},
              {"commentId":"c-2","domainKey":"challenger"},
              {"commentId":"c-3","domainKey":"figma"}
            ]
            """;
        when(chatCompleteUseCase.complete(any())).thenReturn(
            ChatCompletionResult.of(jsonResponse, "google-genai", 300L, 60L)
        );

        Map<String, String> results = classifier.classifyBatch(comments, CANDIDATES);

        assertThat(results)
            .containsEntry("c-1", "auth")
            .containsEntry("c-2", "challenger")
            .containsEntry("c-3", "figma");
        verify(chatCompleteUseCase, times(1)).complete(any());
    }

    @Test
    @DisplayName("classifyBatch 는 캐시된 댓글은 LLM 호출에서 제외하고 미캐시 댓글만 묶어서 호출한다")
    void batch_분류_캐시_혼합() {
        FigmaCommentInfo cached = comment("c-cached", "이전 호출");
        FigmaCommentInfo fresh1 = comment("c-fresh-1", "신규 1");
        FigmaCommentInfo fresh2 = comment("c-fresh-2", "신규 2");

        when(chatCompleteUseCase.complete(any())).thenReturn(
            ChatCompletionResult.of("[{\"commentId\":\"c-cached\",\"domainKey\":\"auth\"}]", "p", 0L, 0L),
            ChatCompletionResult.of(
                "[{\"commentId\":\"c-fresh-1\",\"domainKey\":\"figma\"},"
                    + "{\"commentId\":\"c-fresh-2\",\"domainKey\":\"challenger\"}]",
                "p", 0L, 0L)
        );

        classifier.classifyBatch(List.of(cached), CANDIDATES);
        Map<String, String> second = classifier.classifyBatch(List.of(cached, fresh1, fresh2), CANDIDATES);

        ArgumentCaptor<ChatCompleteCommand> captor = ArgumentCaptor.forClass(ChatCompleteCommand.class);
        verify(chatCompleteUseCase, times(2)).complete(captor.capture());
        ChatCompleteCommand secondCall = captor.getAllValues().get(1);
        assertThat(secondCall.userPrompt()).contains("c-fresh-1").contains("c-fresh-2");
        assertThat(secondCall.userPrompt()).doesNotContain("c-cached");
        assertThat(second)
            .containsEntry("c-cached", "auth")
            .containsEntry("c-fresh-1", "figma")
            .containsEntry("c-fresh-2", "challenger");
    }

    @Test
    @DisplayName("classifyBatch 응답이 JSON 코드 블록(```json) 으로 감싸져 있어도 파싱한다")
    void batch_분류_마크다운_펜스_제거() {
        FigmaCommentInfo comment = comment("c-md", "msg");
        String fenced = """
            ```json
            [{"commentId":"c-md","domainKey":"auth"}]
            ```
            """;
        when(chatCompleteUseCase.complete(any())).thenReturn(
            ChatCompletionResult.of(fenced, "p", 0L, 0L)
        );

        Map<String, String> results = classifier.classifyBatch(List.of(comment), CANDIDATES);

        assertThat(results).containsEntry("c-md", "auth");
    }

    @Test
    @DisplayName("classifyBatch 응답에서 후보 외 domainKey 항목은 결과에 포함되지 않는다")
    void batch_분류_후보_외_제외() {
        FigmaCommentInfo c1 = comment("c-ok", "auth");
        FigmaCommentInfo c2 = comment("c-bad", "?");
        when(chatCompleteUseCase.complete(any())).thenReturn(
            ChatCompletionResult.of(
                "[{\"commentId\":\"c-ok\",\"domainKey\":\"auth\"},"
                    + "{\"commentId\":\"c-bad\",\"domainKey\":\"unknown\"}]",
                "p", 0L, 0L)
        );

        Map<String, String> results = classifier.classifyBatch(List.of(c1, c2), CANDIDATES);

        assertThat(results).containsOnlyKeys("c-ok");
        assertThat(results.get("c-ok")).isEqualTo("auth");
    }

    private FigmaCommentInfo comment(String id, String message) {
        return new FigmaCommentInfo(id, message, "tester", "1:1", Instant.parse("2026-05-07T00:00:00Z"));
    }
}
