package com.umc.product.notice.domain.exception;

import com.umc.product.global.response.code.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum NoticeErrorCode implements BaseCode {


    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
