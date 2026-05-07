package com.umc.product.llm.application.port.in.dto;

import java.util.List;

/**
 * LLM 호출 입력. 분류/요약/자유 응답 어디에서도 동일 형태로 호출되도록 generic 하게 둔다.
 *
 * @param systemPrompt 모델의 역할/제약을 지시하는 system 프롬프트 (없으면 null/empty)
 * @param userPrompt   실제 입력 본문
 * @param candidates   분류 시 LLM 이 그 중 하나만 반환하도록 제한할 후보 라벨 리스트.
 *                     null/empty 면 자유 응답.
 */
public record ChatCompleteCommand(
    String systemPrompt,
    String userPrompt,
    List<String> candidates
) {
    public static ChatCompleteCommand freeForm(String systemPrompt, String userPrompt) {
        return new ChatCompleteCommand(systemPrompt, userPrompt, List.of());
    }

    public static ChatCompleteCommand classify(String systemPrompt, String userPrompt, List<String> candidates) {
        return new ChatCompleteCommand(systemPrompt, userPrompt, candidates);
    }

    public boolean isClassification() {
        return candidates != null && !candidates.isEmpty();
    }
}
