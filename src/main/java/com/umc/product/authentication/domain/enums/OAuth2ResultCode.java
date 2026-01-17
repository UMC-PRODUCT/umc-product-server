package com.umc.product.authentication.domain.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OAuth2ResultCode {
    SUCCESS(true, "LOGIN_SUCCESS", "기존 사용자가 OAuth2로 로그인에 성공했습니다."),
    REGISTER_REQUIRED(true, "REGISTER_REQUIRED", "OAuth2 인증은 성공하였으나, 기존 사용자가 아닙니다. oAuthVerificationToken을 발급합니다."),
    INFO_MISSING(false, "INFO_MISSING", "OAuth2 인증에 필요한 정보가 누락되어 있습니다. 서버팀에 문의해주세요."),
    ;


    private final boolean success;
    private final String code;
    private final String message;
}
