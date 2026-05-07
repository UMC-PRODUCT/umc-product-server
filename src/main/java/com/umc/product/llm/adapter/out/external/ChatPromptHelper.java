package com.umc.product.llm.adapter.out.external;

import com.umc.product.llm.application.port.in.dto.ChatCompleteCommand;

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
     * 모델 응답에서 앞뒤 공백/개행을 제거한다. 후보 매칭은 호출자 책임.
     */
    static String normalizeResponse(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.trim();
    }
}
