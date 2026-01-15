package com.umc.product.fcm.entity.exception;

import com.umc.product.global.response.code.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum FcmErrorCode implements BaseCode {

    FCM_NOT_FOUND(HttpStatus.NOT_FOUND, "FCM-0001", "FCM 토큰을 찾을 수 없습니다."),
    USER_FCM_NOT_FOUND(HttpStatus.NOT_FOUND, "FCM-0002", "해당 유저의 FCM 토큰을 찾을 수 없습니다.")
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
