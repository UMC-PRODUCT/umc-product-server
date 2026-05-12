package com.umc.product.llm.domain.exception;

import com.umc.product.global.response.code.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum LlmErrorCode implements BaseCode {

    CHAT_COMPLETION_FAILED(HttpStatus.BAD_GATEWAY, "LLM-0001", "LLM 호출에 실패했습니다."),
    CHAT_COMPLETION_INVALID_RESPONSE(HttpStatus.BAD_GATEWAY, "LLM-0002", "LLM 응답을 해석할 수 없습니다."),
    PROVIDER_NOT_CONFIGURED(HttpStatus.INTERNAL_SERVER_ERROR, "LLM-0003", "LLM provider 설정이 누락되었습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
