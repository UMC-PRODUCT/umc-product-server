package com.umc.product.figma.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.umc.product.figma.application.port.out.dto.FigmaCommentInfo;
import com.umc.product.llm.application.port.in.ChatCompleteUseCase;
import com.umc.product.llm.application.port.in.dto.ChatCompletionResult;
import com.umc.product.llm.domain.exception.LlmDomainException;
import com.umc.product.llm.domain.exception.LlmErrorCode;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
        classifier = new FigmaCommentDomainClassifier(chatCompleteUseCase);
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

    private FigmaCommentInfo comment(String id, String message) {
        return new FigmaCommentInfo(id, message, "tester", "1:1", Instant.parse("2026-05-07T00:00:00Z"));
    }
}
