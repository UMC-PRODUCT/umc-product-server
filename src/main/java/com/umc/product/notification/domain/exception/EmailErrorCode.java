package com.umc.product.notification.domain.exception;

import com.umc.product.global.response.code.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum EmailErrorCode implements BaseCode {
    EMAIL_TEMPLATE_RENDER_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "EMAIL-0004", "이메일 본문을 만들지 못했어요. 관리자에게 문의해주세요."),
    EMAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "EMAIL-0005", "이메일을 보내지 못했어요. 잠시 후 다시 시도해주세요."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
