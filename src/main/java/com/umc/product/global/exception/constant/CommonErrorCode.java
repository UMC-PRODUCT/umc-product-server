package com.umc.product.global.exception.constant;


import com.umc.product.global.response.code.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 프로젝트 전체에서 공통으로 사용되는 COMMON 카테고리의 에러입니다.
 */
@Getter
@AllArgsConstructor
public enum CommonErrorCode implements BaseCode {

    // Error Code는 DOMAIN-CATEGORY-NUMBER 형식으로 작성할 것.
    // 에러 코드는 되도록 재사용 금지

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON-0001",
            "예상하지 않은 오류입니다. 관리자에게 문의해주세요."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON-400", "잘못된 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON-401", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON-403", "허용되지 않는 요청입니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}