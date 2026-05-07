package com.umc.product.llm.adapter.out.external;

import com.umc.product.llm.application.port.in.dto.ChatCompleteCommand;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;

/**
 * Spring AI 어댑터들이 공유하는 프롬프트 / 응답 정규화 헬퍼.
 * provider 별 어댑터가 동일한 분류 의미와 정규화 규칙을 사용하도록 한 곳에 모아 둔다.
 */
final class ChatPromptHelper {

    private ChatPromptHelper() {
    }

    /**
     * 분류 호출일 때 system prompt 끝에 "반드시 후보 중 하나만 반환" 제약을 강하게 덧붙인다.
     * 자유 응답 호출은 system prompt 를 그대로 반환한다.
     */
    static String buildSystemPrompt(ChatCompleteCommand command) {
        String base = command.systemPrompt() == null ? "" : command.systemPrompt();
        if (!command.isClassification()) {
            return base;
        }
        String candidatesLine = String.join(", ", command.candidates());
        return base + "\n\n[추가 제약]\n반드시 다음 후보 중 정확히 하나만, 다른 단어나 설명 없이 그 후보 문자열만 응답하라.\n후보: " + candidatesLine;
    }

    /**
     * 호출 단위 max-tokens 해석. command 의 override 가 있으면 그 값을, 없으면 properties 기본값을 반환한다.
     */
    static int resolveMaxOutputTokens(ChatCompleteCommand command, LlmProperties properties) {
        Integer override = command.maxOutputTokensOverride();
        if (override != null && override > 0) {
            return override;
        }
        return properties.maxOutputTokens();
    }

    /**
     * 모델 응답에서 앞뒤 공백/개행을 제거한다. 후보 매칭은 호출자 책임.
     */
    static String normalizeResponse(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.trim();
    }

    /**
     * Spring AI ChatResponse 메타데이터에서 입력 토큰 수를 추출한다. 부재 시 null.
     */
    static Long extractPromptTokens(ChatResponse response) {
        Usage usage = extractUsage(response);
        if (usage == null || usage.getPromptTokens() == null) {
            return null;
        }
        return usage.getPromptTokens().longValue();
    }

    /**
     * Spring AI ChatResponse 메타데이터에서 출력 토큰 수를 추출한다. 부재 시 null.
     */
    static Long extractCompletionTokens(ChatResponse response) {
        Usage usage = extractUsage(response);
        if (usage == null || usage.getCompletionTokens() == null) {
            return null;
        }
        return usage.getCompletionTokens().longValue();
    }

    private static Usage extractUsage(ChatResponse response) {
        if (response == null) {
            return null;
        }
        ChatResponseMetadata metadata = response.getMetadata();
        if (metadata == null) {
            return null;
        }
        return metadata.getUsage();
    }
}
