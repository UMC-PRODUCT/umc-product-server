package com.umc.product.notification.domain.exception;

import org.springframework.http.HttpStatus;

import com.umc.product.global.response.code.BaseCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FcmErrorCode implements BaseCode {

    FCM_NOT_FOUND(HttpStatus.NOT_FOUND, "FCM-0001", "푸시 알림 정보를 찾을 수 없어요. 알림 설정을 다시 확인해주세요."),
    USER_FCM_NOT_FOUND(HttpStatus.NOT_FOUND, "FCM-0002", "사용자의 푸시 알림 정보를 찾을 수 없어요. 알림 설정을 확인해주세요."),
    FCM_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FCM-0003", "푸시 알림을 보내지 못했어요. 잠시 후 다시 시도해주세요."),
    TOPIC_SUBSCRIBE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FCM-0004", "푸시 알림 주제를 구독하지 못했어요. 잠시 후 다시 시도해주세요."),
    TOPIC_UNSUBSCRIBE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FCM-0005", "푸시 알림 주제 구독을 해제하지 못했어요. 잠시 후 다시 시도해주세요."),
    TOPIC_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FCM-0006", "푸시 알림 주제 메시지를 보내지 못했어요. 잠시 후 다시 시도해주세요."),
    RATE_LIMITED(HttpStatus.TOO_MANY_REQUESTS, "FCM-0007", "푸시 알림 요청이 너무 많아요. 잠시 후 다시 시도해주세요."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
