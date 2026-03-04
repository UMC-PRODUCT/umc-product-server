package com.umc.product.notification.domain.exception;

import com.umc.product.global.response.code.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum WebhookErrorCode implements BaseCode {

    WEBHOOK_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "WEBHOOK-0001", "웹훅 메시지 전송에 실패했습니다."),
    WEBHOOK_ADAPTER_NOT_FOUND(HttpStatus.BAD_REQUEST, "WEBHOOK-0002", "해당 플랫폼의 웹훅 어댑터가 등록되지 않았습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
