package com.umc.product.test.domain.exception;

import org.springframework.http.HttpStatus;

import com.umc.product.global.response.code.BaseCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SeedErrorCode implements BaseCode {

    ACCOUNT_NOT_SEEDED(HttpStatus.BAD_REQUEST, "SEED-0101", "QA 계정이 없어요. QA 시딩(/test/seed/qa)을 먼저 실행해주세요."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
