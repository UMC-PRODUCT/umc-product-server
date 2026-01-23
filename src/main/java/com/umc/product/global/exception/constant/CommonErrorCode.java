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
    // e.g. CHALLENGER-COMMON-0001
    // 카테고리는 도메인 내의 세부 카테고리, 작성자에게 권한을 드립니다.
    // Number는 0001 부터 4자리로 작성하며, 삭제할 경우 결번 처리하여 중복을 방지해주세요.
    // 반드시 Notion 및 Docs와 동기화해주세요.
    // 에러 코드는 되도록 재사용 금지

    // COMMON: 일반 상태 코드
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON-0001",
            "알 수 없는 오류입니다. 관리자에게 문의해주세요."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON-400", "잘못된 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON-401", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON-403", "허용되지 않는 요청입니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON-404", "요청한 리소스를 찾을 수 없습니다."),
    NOT_IMPLEMENTED(HttpStatus.NOT_IMPLEMENTED, "COMMON-501", "아직 구현되지 않은 기능입니다. 서버팀에게 문의해주세요."),

    // SECURITY: Spring Security에서 발생하는 에러
    SECURITY_NOT_GIVEN(HttpStatus.UNAUTHORIZED, "SECURITY-0001", "인증 정보가 전달되지 않았습니다."),
    SECURITY_FORBIDDEN(HttpStatus.FORBIDDEN, "SECURITY-0002", "권한이 부족합니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
