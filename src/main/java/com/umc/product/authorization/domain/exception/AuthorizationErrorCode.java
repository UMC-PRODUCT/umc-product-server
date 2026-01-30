package com.umc.product.authorization.domain.exception;

import com.umc.product.global.response.code.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthorizationErrorCode implements BaseCode {

    PERMISSION_DENIED(HttpStatus.FORBIDDEN, "AUTHORIZATION-0001", "권한이 없습니다."),
    RESOURCE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "AUTHORIZATION-0002", "해당 리소스에 접근할 권한이 없습니다."),
    INVALID_PERMISSION(HttpStatus.BAD_REQUEST, "AUTHORIZATION-0003", "유효하지 않은 권한입니다."),
    POLICY_EVALUATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AUTHORIZATION-0004", "권한 검증 중 오류가 발생했습니다."),
    NO_EVALUATOR_MATCHING_RESOURCE_TYPE(HttpStatus.INTERNAL_SERVER_ERROR, "AUTHORIZATION-0005",
        "해당 리소스 타입에 해당하는 Permission Evaluator가 존재하지 않습니다. 관리자에게 문의하세요."),
    PERMISSION_TYPE_NOT_SUPPORTED_BY_RESOURCE_TYPE(HttpStatus.INTERNAL_SERVER_ERROR, "AUTHORIZATION-0006",
        "리소스 유형에서 지원하지 않는 권한 유형을 검사하고자 시도하였습니다. 관리자에게 문의하세요."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
