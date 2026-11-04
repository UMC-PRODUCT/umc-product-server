package com.umc.product.test.domain.exception;

import org.springframework.http.HttpStatus;

import com.umc.product.global.response.code.BaseCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SeedErrorCode implements BaseCode {

    // 차수 넘기기
    PREVIOUS_ROUND_NOT_PUBLISHED(HttpStatus.CONFLICT, "SEED-0301",
        "앞 차수 결과가 아직 발표되지 않았어요. 앞 차수를 먼저 발표로 넘겨주세요."),
    AUTO_DECISION_NOT_EXECUTED(HttpStatus.CONFLICT, "SEED-0302",
        "자동 선발이 아직 실행되지 않았어요. 선발 마감으로 넘긴 뒤 1분 정도 기다렸다가 다시 시도해주세요."),
    STEP_OUT_OF_ORDER(HttpStatus.CONFLICT, "SEED-0303",
        "차수 단계는 한 칸씩만 넘길 수 있어요. 앞 단계를 먼저 넘겨주세요."),
    RANDOM_NOT_ASSIGNED(HttpStatus.CONFLICT, "SEED-0304",
        "랜덤 배정이 아직 실행되지 않았어요. 3차 발표 뒤 1분 정도 기다렸다가 다시 시도해주세요."),
    ROUND_NOT_FOUND(HttpStatus.NOT_FOUND, "SEED-0307", "매칭 차수를 찾을 수 없어요. 차수 ID를 확인해주세요."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
