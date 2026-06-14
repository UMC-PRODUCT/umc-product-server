package com.umc.product.documentation.domain;

import org.springframework.http.HttpStatus;

import com.umc.product.global.response.code.BaseCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DocumentationErrorCode implements BaseCode {

    ERROR_CODE_CATALOG_UNAVAILABLE(HttpStatus.INTERNAL_SERVER_ERROR, "DOCS-0001",
        "에러 코드 목록을 불러오지 못했어요. 잠시 후 다시 시도해주세요."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
