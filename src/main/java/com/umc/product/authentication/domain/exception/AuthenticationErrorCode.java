package com.umc.product.authentication.domain.exception;

import com.umc.product.global.response.code.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthenticationErrorCode implements BaseCode {

    // JWT: JWT 관련 에러
    WRONG_JWT_SIGNATURE(HttpStatus.FORBIDDEN, "AUTHENTICATION-JWT-0001", "JWT 토큰의 서명이 잘못되었습니다."),
    EXPIRED_JWT_TOKEN(HttpStatus.FORBIDDEN, "AUTHENTICATION-JWT-0002", "만료된 JWT 토큰입니다."),
    UNSUPPORTED_JWT(HttpStatus.FORBIDDEN, "AUTHENTICATION-JWT-0003", "지원하지 않는 JWT 토큰입니다."),
    INVALID_JWT(HttpStatus.FORBIDDEN, "AUTHENTICATION-JWT-0004", "잘못된 JWT 토큰입니다."),
    // AUTHENTICATION: 인증 관련 에러
    OAUTH_PROVIDER_NOT_FOUND(HttpStatus.BAD_REQUEST, "AUTHENTICATION-0001", "지원하지 않은 OAuth 제공자입니다."),
    NO_MATCHING_MEMBER(HttpStatus.NOT_FOUND, "AUTHENTICATION-0002",
            "제공된 OAuth Provider와 ProviderId와 일치하는 회원이 존재하지 않습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
