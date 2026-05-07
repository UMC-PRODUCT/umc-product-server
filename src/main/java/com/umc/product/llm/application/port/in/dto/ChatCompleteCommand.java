package com.umc.product.llm.application.port.in.dto;

import java.util.List;

/**
 * LLM 호출 입력. 분류/요약/자유 응답 어디에서도 동일 형태로 호출되도록 generic 하게 둔다.
 *
 * @param systemPrompt            모델의 역할/제약을 지시하는 system 프롬프트 (없으면 null/empty)
 * @param userPrompt              실제 입력 본문
 * @param candidates              분류 시 LLM 이 그 중 하나만 반환하도록 제한할 후보 라벨 리스트.
 *                                null/empty 면 자유 응답.
 * @param maxOutputTokensOverride 호출 단위로 응답 토큰 한도를 override 한다. null 이면
 *                                {@link com.umc.product.llm.adapter.out.external.LlmProperties#maxOutputTokens()}
 *                                를 사용. batch 분류처럼 응답이 길어지는 호출에서만 명시한다.
 */
public record ChatCompleteCommand(
    String systemPrompt,
    String userPrompt,
    List<String> candidates,
    Integer maxOutputTokensOverride
) {
    public static ChatCompleteCommand freeForm(String systemPrompt, String userPrompt) {
        return new ChatCompleteCommand(systemPrompt, userPrompt, List.of(), null);
    }

    public static ChatCompleteCommand classify(String systemPrompt, String userPrompt, List<String> candidates) {
        return new ChatCompleteCommand(systemPrompt, userPrompt, candidates, null);
    }

    /**
     * 자유 응답 호출이지만 응답 토큰 한도를 명시적으로 늘려야 하는 경우 (예: 다중 댓글 batch JSON 응답).
     * candidates 는 비워두고 후보 검증은 호출자 책임으로 둔다.
     */
    public static ChatCompleteCommand freeFormWithMaxTokens(String systemPrompt, String userPrompt, int maxOutputTokens) {
        return new ChatCompleteCommand(systemPrompt, userPrompt, List.of(), maxOutputTokens);
    }

    public boolean isClassification() {
        return candidates != null && !candidates.isEmpty();
    }
}
