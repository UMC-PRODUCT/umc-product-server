package com.umc.product.challenger.exception;

import com.umc.product.global.response.code.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ChallengerErrorCode implements BaseCode {

    INVALID_WORKBOOK_STATUS(HttpStatus.BAD_REQUEST, "CHALLENGER-0001", "유효하지 않은 워크북 상태입니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
