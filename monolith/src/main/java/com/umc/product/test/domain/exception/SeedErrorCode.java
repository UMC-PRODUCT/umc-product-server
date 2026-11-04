package com.umc.product.test.domain.exception;

import org.springframework.http.HttpStatus;

import com.umc.product.global.response.code.BaseCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SeedErrorCode implements BaseCode {

    ACCOUNT_NOT_SEEDED(HttpStatus.BAD_REQUEST, "SEED-0101", "QA 계정이 없어요. QA 시딩(/test/seed/qa)을 먼저 실행해주세요."),
    NOT_IN_APPLICATION_WINDOW(HttpStatus.CONFLICT, "SEED-0305",
        "지원 기간이 아니에요. 봇 지원은 차수 지원 시작부터 지원 마감 전까지만 부를 수 있어요."),
    NOT_IN_DECISION_WINDOW(HttpStatus.CONFLICT, "SEED-0306",
        "PM 결정 기간이 아니에요. 봇 결정은 지원 마감 뒤부터 선발 마감 전, 자동 선발 전까지만 부를 수 있어요."),
    ROUND_NOT_FOUND(HttpStatus.NOT_FOUND, "SEED-0307", "매칭 차수를 찾을 수 없어요. 차수 ID를 확인해주세요."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
