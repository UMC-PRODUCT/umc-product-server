package com.umc.product.terms.domain.exception;

import com.umc.product.global.response.code.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum TermsErrorCode implements BaseCode {

    TERMS_NOT_FOUND(HttpStatus.NOT_FOUND, "TERMS-0001", "약관을 찾을 수 없습니다."),
    TERMS_TYPE_REQUIRED(HttpStatus.BAD_REQUEST, "TERMS-0002", "약관 타입은 필수입니다."),
    TERMS_TITLE_REQUIRED(HttpStatus.BAD_REQUEST, "TERMS-0003", "약관 제목은 필수입니다."),
    TERMS_CONTENT_REQUIRED(HttpStatus.BAD_REQUEST, "TERMS-0004", "약관 내용은 필수입니다."),
    TERMS_VERSION_REQUIRED(HttpStatus.BAD_REQUEST, "TERMS-0005", "약관 버전은 필수입니다."),

    TERMS_CONSENT_NOT_FOUND(HttpStatus.NOT_FOUND, "TERMS-0006", "약관 동의 정보를 찾을 수 없습니다."),
    TERMS_CONSENT_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "TERMS-0007", "이미 동의한 약관입니다."),
    MEMBER_ID_REQUIRED(HttpStatus.BAD_REQUEST, "TERMS-0008", "회원 ID는 필수입니다."),
    TERM_ID_REQUIRED(HttpStatus.BAD_REQUEST, "TERMS-0009", "약관 ID는 필수입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
