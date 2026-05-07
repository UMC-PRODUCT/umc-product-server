package com.umc.product.llm.application.port.out;

import com.umc.product.llm.application.port.in.dto.ChatCompleteCommand;
import com.umc.product.llm.application.port.in.dto.ChatCompletionResult;

/**
 * LLM provider 추상. 구현체로는 Mock / OpenAI / Gemini / Spring AI 등이 가능하다. 어떤 provider 가 활성화될지는 application property 와 어댑터의
 * ConditionalOnProperty 가 결정한다.
 */
public interface ChatCompletionPort {

    ChatCompletionResult complete(ChatCompleteCommand command);

    /**
     * 이 어댑터가 응답에 표기할 provider 식별자.
     */
    String providerName();
}
