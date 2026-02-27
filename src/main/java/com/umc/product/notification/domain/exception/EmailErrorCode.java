package com.umc.product.notification.domain.exception;

import com.umc.product.global.response.code.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum EmailErrorCode implements BaseCode {
    EMAIL_GENERAL_ERROR(HttpStatus.BAD_REQUEST, "EMAIL-0001", "알 수 없는 사유로 이메일 전송에 실패했습니다."),
    EMAIL_ENCODING_ERROR(HttpStatus.BAD_REQUEST, "EMAIL-0002", "인코딩 과정에서 오류가 발생했습니다."),
    EMAIL_MESSAGING_ERROR(HttpStatus.BAD_REQUEST, "EMAIL-0003", "메일 전송 과정에서 오류가 발생했습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
