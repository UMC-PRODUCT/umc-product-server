package com.umc.product.llm.adapter.out.external;

import com.umc.product.llm.application.port.in.dto.ChatCompleteCommand;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;

/**
 * Spring AI 어댑터들이 공유하는 응답 정규화 / 토큰 추출 헬퍼.
 * <p>
 * prompt 자체에는 손대지 않는다. system/user prompt 의 의미는 호출자가 완성해 보내며
 * LLM 어댑터는 ChatModel 호출과 응답 파싱만 담당한다.
 */
final class ChatPromptHelper {

    private ChatPromptHelper() {
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
     * 모델 응답에서 앞뒤 공백/개행을 제거한다. 의미적 검증/매칭은 호출자 책임.
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
