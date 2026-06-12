package com.umc.product.notification.domain.exception;

import org.springframework.http.HttpStatus;

import com.umc.product.global.response.code.BaseCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WebhookErrorCode implements BaseCode {

    WEBHOOK_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "WEBHOOK-0001", "웹훅 메시지를 보내지 못했어요. 잠시 후 다시 시도해주세요."),
    WEBHOOK_ADAPTER_NOT_FOUND(HttpStatus.BAD_REQUEST, "WEBHOOK-0002", "해당 플랫폼의 웹훅 설정을 찾을 수 없어요. 플랫폼 설정을 확인해주세요."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
