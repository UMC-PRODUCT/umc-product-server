package com.umc.product.llm.application.port.in;

import com.umc.product.llm.application.port.in.dto.ChatCompleteCommand;
import com.umc.product.llm.application.port.in.dto.ChatCompletionResult;

public interface ChatCompleteUseCase {

    /**
     * 입력 프롬프트에 대한 LLM 응답을 반환한다. provider 선택은 ChatCompletionPort 구현체에 위임된다.
     */
    ChatCompletionResult complete(ChatCompleteCommand command);
}
