package com.umc.product.llm.application.port.in.dto;

/**
 * LLM 응답.
 *
 * @param text     원본 응답 텍스트
 * @param provider 응답을 생성한 provider 식별자 ("mock", "openai", "gemini" 등)
 */
public record ChatCompletionResult(
    String text,
    String provider
) {
    public static ChatCompletionResult of(String text, String provider) {
        return new ChatCompletionResult(text, provider);
    }
}
