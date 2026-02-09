package com.umc.product.notification.domain.exception;

import com.umc.product.global.response.code.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum FcmErrorCode implements BaseCode {

    FCM_NOT_FOUND(HttpStatus.NOT_FOUND, "FCM-0001", "FCM 토큰을 찾을 수 없습니다."),
    USER_FCM_NOT_FOUND(HttpStatus.NOT_FOUND, "FCM-0002", "해당 유저의 FCM 토큰을 찾을 수 없습니다."),
    FCM_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FCM-0003", "FCM 메시지 전송에 실패했습니다."),
    TOPIC_SUBSCRIBE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FCM-0004", "FCM 토픽 구독에 실패했습니다."),
    TOPIC_UNSUBSCRIBE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FCM-0005", "FCM 토픽 구독 해제에 실패했습니다."),
    TOPIC_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FCM-0006", "FCM 토픽 메시지 전송에 실패했습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
