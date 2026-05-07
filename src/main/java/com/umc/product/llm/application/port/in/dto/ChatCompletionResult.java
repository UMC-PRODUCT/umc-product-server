package com.umc.product.llm.application.port.in.dto;

/**
 * LLM 응답.
 *
 * @param text             원본 응답 텍스트
 * @param provider         응답을 생성한 provider 식별자 ("mock", "openai", "gemini" 등)
 * @param promptTokens     입력 토큰 수 (provider 가 제공하지 않으면 null)
 * @param completionTokens 출력 토큰 수 (provider 가 제공하지 않으면 null)
 */
public record ChatCompletionResult(
    String text,
    String provider,
    Long promptTokens,
    Long completionTokens
) {
    public static ChatCompletionResult of(String text, String provider) {
        return new ChatCompletionResult(text, provider, null, null);
    }

    public static ChatCompletionResult of(String text, String provider, Long promptTokens, Long completionTokens) {
        return new ChatCompletionResult(text, provider, promptTokens, completionTokens);
    }
}
