package com.umc.product.documentation.domain;

import org.springframework.http.HttpStatus;

import com.umc.product.global.exception.documentation.ErrorCodeRetryable;
import com.umc.product.global.exception.documentation.ErrorCodeSeverity;
import com.umc.product.global.exception.documentation.ErrorCodeSpec;
import com.umc.product.global.response.code.BaseCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DocumentationErrorCode implements BaseCode {

    @ErrorCodeSpec(
        description = "Generated ErrorCode catalog resource is missing, unreadable, or invalid.",
        clientAction = "Retry after the server has regenerated and redeployed the catalog.",
        retryable = ErrorCodeRetryable.TRUE,
        severity = ErrorCodeSeverity.ERROR,
        owners = {"server"},
        tags = {"documentation", "backoffice"}
    )
    ERROR_CODE_CATALOG_UNAVAILABLE(HttpStatus.INTERNAL_SERVER_ERROR, "DOCS-0001",
        "ErrorCode 카탈로그를 불러오지 못했어요. 잠시 후 다시 시도해주세요."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
