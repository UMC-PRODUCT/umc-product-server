package com.umc.product.llm.domain.exception;

import com.umc.product.global.response.code.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum LlmErrorCode implements BaseCode {

    CHAT_COMPLETION_FAILED(HttpStatus.BAD_GATEWAY, "LLM-0001", "AI 응답을 생성하지 못했어요. 잠시 후 다시 시도해주세요."),
    CHAT_COMPLETION_INVALID_RESPONSE(HttpStatus.BAD_GATEWAY, "LLM-0002", "AI 응답을 읽지 못했어요. 잠시 후 다시 시도해주세요."),
    PROVIDER_NOT_CONFIGURED(HttpStatus.INTERNAL_SERVER_ERROR, "LLM-0003", "AI 제공자 설정이 누락됐어요. 관리자에게 문의해주세요."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
