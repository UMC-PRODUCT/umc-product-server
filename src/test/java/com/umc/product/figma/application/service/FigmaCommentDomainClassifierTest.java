package com.umc.product.figma.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.product.figma.application.port.out.LoadFigmaCommentClassificationPort;
import com.umc.product.figma.application.port.out.SaveFigmaCommentClassificationPort;
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

    @Mock
    private LoadFigmaCommentClassificationPort loadClassificationPort;

    @Mock
    private SaveFigmaCommentClassificationPort saveClassificationPort;

    private FigmaCommentDomainClassifier classifier;

    @BeforeEach
    void setUp() {
        classifier = new FigmaCommentDomainClassifier(
            chatCompleteUseCase, new ObjectMapper(),
            loadClassificationPort, saveClassificationPort
        );
        // 기본은 DB 미스 (영구 캐시 비어 있음)
        lenient().when(loadClassificationPort.findClassifications(anyCollection())).thenReturn(Map.of());
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
    @DisplayName("LLM 호출 자체가 실패하면 null 을 반환하지만 negative 캐시는 박지 않아 다음 호출이 LLM 을 재시도한다")
    void LLM_호출_실패_시_negative_캐시_미설정_재시도() {
        FigmaCommentInfo comment = comment("c-3", "msg");
        when(chatCompleteUseCase.complete(any()))
            .thenThrow(new LlmDomainException(LlmErrorCode.CHAT_COMPLETION_FAILED));

        String first = classifier.classify(comment, CANDIDATES);
        String second = classifier.classify(comment, CANDIDATES);

        assertThat(first).isNull();
        assertThat(second).isNull();
        // P1 fix: transient 호출 실패는 캐시되지 않으므로 두 번째 호출이 LLM 을 다시 시도해야 한다.
        verify(chatCompleteUseCase, times(2)).complete(any());
    }

    @Test
    @DisplayName("classifyBatch 도 LLM 호출 자체 실패면 negative 캐시를 박지 않고 다음 호출이 재시도된다")
    void classifyBatch_호출_실패_시_negative_캐시_미설정_재시도() {
        FigmaCommentInfo c1 = comment("c-batch-fail-1", "msg1");
        FigmaCommentInfo c2 = comment("c-batch-fail-2", "msg2");
        when(chatCompleteUseCase.complete(any()))
            .thenThrow(new LlmDomainException(LlmErrorCode.CHAT_COMPLETION_FAILED));

        Map<String, String> first = classifier.classifyBatch(List.of(c1, c2), CANDIDATES);
        Map<String, String> second = classifier.classifyBatch(List.of(c1, c2), CANDIDATES);

        assertThat(first).isEmpty();
        assertThat(second).isEmpty();
        // P1 fix: bulk 호출 실패도 캐시되지 않아 다음 사이클에 LLM batch 가 다시 호출되어야 한다.
        verify(chatCompleteUseCase, times(2)).complete(any());
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

    @Test
    @DisplayName("DB 영구 캐시 히트면 LLM 을 호출하지 않고 그 값을 반환한다")
    void DB_캐시_히트_시_LLM_미호출() {
        FigmaCommentInfo comment = comment("c-db-hit", "msg");
        when(loadClassificationPort.findClassifications(anyCollection()))
            .thenReturn(Map.of("c-db-hit", "challenger"));

        String first = classifier.classify(comment, CANDIDATES);
        // 두 번째 호출은 L1 캐시 hit 으로 DB 도 안 거침
        String second = classifier.classify(comment, CANDIDATES);

        assertThat(first).isEqualTo("challenger");
        assertThat(second).isEqualTo("challenger");
        verify(chatCompleteUseCase, never()).complete(any());
    }

    @Test
    @DisplayName("classifyBatch 에서 일부 댓글이 DB 영구 캐시에 있으면 그것만 제외하고 LLM 에 보낸다")
    void batch_분류_DB_부분_히트() {
        FigmaCommentInfo persisted = comment("c-persisted", "이전");
        FigmaCommentInfo fresh = comment("c-fresh", "신규");
        when(loadClassificationPort.findClassifications(anyCollection()))
            .thenReturn(Map.of("c-persisted", "auth"));
        when(chatCompleteUseCase.complete(any())).thenReturn(
            ChatCompletionResult.of(
                "[{\"commentId\":\"c-fresh\",\"domainKey\":\"figma\"}]",
                "openai", 0L, 0L)
        );

        Map<String, String> results = classifier.classifyBatch(List.of(persisted, fresh), CANDIDATES);

        ArgumentCaptor<ChatCompleteCommand> captor = ArgumentCaptor.forClass(ChatCompleteCommand.class);
        verify(chatCompleteUseCase, times(1)).complete(captor.capture());
        assertThat(captor.getValue().userPrompt())
            .contains("c-fresh")
            .doesNotContain("c-persisted");
        assertThat(results)
            .containsEntry("c-persisted", "auth")
            .containsEntry("c-fresh", "figma");
    }

    @Test
    @DisplayName("정상 분류는 영구 캐시에 저장된다 (실 provider)")
    void 정상_분류_영구_캐시_저장() {
        FigmaCommentInfo comment = comment("c-save", "auth 댓글");
        when(chatCompleteUseCase.complete(any())).thenReturn(
            ChatCompletionResult.of("auth", "openai", 0L, 0L)
        );

        classifier.classify(comment, CANDIDATES);

        verify(saveClassificationPort, times(1)).save("c-save", "auth", "openai");
    }

    @Test
    @DisplayName("mock provider 응답은 영구 캐시에 저장하지 않는다 (검증 단계 임시 분류 누적 방지)")
    void mock_provider_영구_캐시_미저장() {
        FigmaCommentInfo comment = comment("c-mock", "msg");
        when(chatCompleteUseCase.complete(any())).thenReturn(
            ChatCompletionResult.of("auth", "mock", 0L, 0L)
        );

        classifier.classify(comment, CANDIDATES);

        verify(saveClassificationPort, never()).save(any(), any(), any());
    }

    @Test
    @DisplayName("classify 단일 호출은 LLM 도메인에 분류 모드를 노출하지 않는다 (freeForm 형태로 전달)")
    void 단일_호출_freeForm_으로_전달() {
        FigmaCommentInfo comment = comment("c-form", "auth 댓글");
        when(chatCompleteUseCase.complete(any())).thenReturn(
            ChatCompletionResult.of("auth", "openai", 0L, 0L)
        );

        classifier.classify(comment, CANDIDATES);

        ArgumentCaptor<ChatCompleteCommand> captor = ArgumentCaptor.forClass(ChatCompleteCommand.class);
        verify(chatCompleteUseCase, times(1)).complete(captor.capture());
        ChatCompleteCommand sent = captor.getValue();
        // figma 도메인이 system prompt 안에 후보 제약과 후보 목록을 모두 담아 보낸다.
        // LLM 도메인 추상은 분류 모드 메타데이터를 알지 않는다.
        assertThat(sent.systemPrompt()).contains("후보 도메인 키").contains("정확히 하나만");
        assertThat(sent.userPrompt()).contains("auth, challenger, figma");
    }

    @Test
    @DisplayName("후보 외 응답은 영구 캐시에 저장하지 않는다 (운영 시점 후보 변경에 안전)")
    void 후보_외_응답_영구_캐시_미저장() {
        FigmaCommentInfo comment = comment("c-out", "?");
        when(chatCompleteUseCase.complete(any())).thenReturn(
            ChatCompletionResult.of("unknown-key", "openai", 0L, 0L)
        );

        classifier.classify(comment, CANDIDATES);

        verify(saveClassificationPort, never()).save(any(), any(), any());
    }

    private FigmaCommentInfo comment(String id, String message) {
        return new FigmaCommentInfo(id, message, "tester", "1:1", Instant.parse("2026-05-07T00:00:00Z"));
    }
}
