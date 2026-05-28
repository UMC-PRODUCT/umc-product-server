package com.umc.product.notification.domain.exception;

import com.umc.product.global.response.code.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum EmailErrorCode implements BaseCode {
    EMAIL_TEMPLATE_RENDER_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "EMAIL-0004", "이메일 본문 템플릿 렌더링에 실패했습니다."),
    EMAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "EMAIL-0005", "이메일 발송에 실패했습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
