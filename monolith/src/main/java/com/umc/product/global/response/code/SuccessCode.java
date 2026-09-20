package com.umc.product.global.response.code;


import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SuccessCode implements BaseCode {

    // 일반적인 응답
    _OK(HttpStatus.OK, "COMMON200", "성공입니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
