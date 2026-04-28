package com.umc.product.authentication.domain.exception;

import com.umc.product.global.response.code.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthenticationErrorCode implements BaseCode {


    // JWT: JWT 관련 에러
    WRONG_JWT_SIGNATURE(HttpStatus.UNAUTHORIZED, "JWT-0001", "JWT 토큰의 서명이 잘못되었습니다."),
    EXPIRED_JWT_TOKEN(HttpStatus.UNAUTHORIZED, "JWT-0002", "만료된 JWT 토큰입니다."),
    UNSUPPORTED_JWT(HttpStatus.UNAUTHORIZED, "JWT-0003", "지원하지 않는 JWT 토큰입니다."),
    INVALID_JWT(HttpStatus.UNAUTHORIZED, "JWT-0004", "잘못된 JWT 토큰입니다."),

    // AUTHENTICATION: 인증 관련 에러
    OAUTH_PROVIDER_NOT_FOUND(HttpStatus.BAD_REQUEST, "AUTHENTICATION-0001", "지원하지 않은 OAuth 제공자입니다."),
    NO_MATCHING_MEMBER(HttpStatus.NOT_FOUND, "AUTHENTICATION-0002",
        "제공된 OAuth Provider와 ProviderId와 일치하는 회원이 존재하지 않습니다."),
    NO_EMAIL_VERIFICATION_METHOD_GIVEN(HttpStatus.BAD_REQUEST, "AUTHENTICATION-0003",
        "잘못된 이메일 요청 인증입니다."),
    INVALID_EMAIL_VERIFICATION(HttpStatus.UNAUTHORIZED, "AUTHENTICATION-0004",
        "이메일 인증 정보가 일치하지 않습니다."),
    UNSUPPORTED_EMAIL_VERIFICATION_METHOD(HttpStatus.BAD_REQUEST, "AUTHENTICATION-0005",
        "지원하지 않는 이메일 인증 방식입니다."),
    ALREADY_VERIFIED_EMAIL(HttpStatus.BAD_REQUEST, "AUTHENTICATION-0017",
        "이미 인증이 완료된 이메일 인증 세션입니다."),
    EMAIL_VERIFICATION_SESSION_EXPIRED(HttpStatus.BAD_REQUEST, "AUTHENTICATION-0018",
        "만료된 이메일 인증 세션입니다. 새로운 인증을 요청해주세요."),

    // OAUTH 관련 에러
    OAUTH_SUCCESS_BUT_NO_MEMBER(HttpStatus.NOT_FOUND, "AUTHENTICATION-0006",
        "OAuth 인증은 성공하였으나, 가입된 회원이 없습니다. oAuthVerificationToken을 확인하세요."),
    OAUTH_SUCCESS_BUT_NO_INFO(HttpStatus.SERVICE_UNAVAILABLE, "AUTHENTICATION-0007",
        "OAuth 인증은 성공하였으나, 필요한 사용자 정보를 제공받지 못했습니다. 관리자에게 문의하세요."),
    OAUTH_FAILURE(HttpStatus.BAD_REQUEST, "AUTHENTICATION-0008", "OAuth 인증에 실패하였습니다."),
    OAUTH_INVALID_ACCESS_TOKEN(HttpStatus.BAD_REQUEST, "AUTHENTICATION-0009", "유효하지 않은 OAuth측 AccessToken 입니다."),
    OAUTH_TOKEN_VERIFICATION_FAILED(HttpStatus.UNAUTHORIZED, "AUTHENTICATION-0010",
        "OAuth 토큰 검증에 실패하였습니다. 관리자에게 문의하세요."),
    INVALID_OAUTH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTHENTICATION-0011", "유효하지 않은 OAuth 토큰입니다."),
    OAUTH_ALREADY_LINKED(HttpStatus.UNAUTHORIZED, "AUTHENTICATION-0012", "이미 다른 계정에 연동된 OAuth 계정입니다."),
    OAUTH_PROVIDER_ALREADY_LINKED(HttpStatus.UNAUTHORIZED, "AUTHENTICATION-0013",
        "해당 계정에 이미 연동된 OAuth 제공자입니다. 기존에 연결된 계정을 해제하고 다시 시도해주세요."),
    MEMBER_OAUTH_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTHENTICATION-0014", "해당하는 Member OAuth가 존재하지 않습니다."),
    NOT_VALID_MEMBER(HttpStatus.FORBIDDEN, "AUTHENTICATION-0015", "해당 작업을 할 권한이 없는 사용자입니다."),
    OAUTH_CANNOT_UNLINK_LAST_PROVIDER(HttpStatus.BAD_REQUEST, "AUTHENTICATION-0016",
        "게정과 연동된 유일한 OAuth는 연동 해제할 수 없습니다. 회원 탈퇴를 이용해주세요."),

    // ID/PW 자격증명 관련 에러
    LOGIN_ID_ALREADY_EXISTS(HttpStatus.CONFLICT, "AUTHENTICATION-0019", "이미 사용 중인 로그인 ID입니다."),
    INVALID_LOGIN_ID_FORMAT(HttpStatus.BAD_REQUEST, "AUTHENTICATION-0020",
        "로그인 ID 형식이 올바르지 않습니다. 영문/숫자/._- 5~20자로 입력해주세요."),
    PASSWORD_POLICY_VIOLATION(HttpStatus.BAD_REQUEST, "AUTHENTICATION-0021",
        "비밀번호는 8~64자이며 영문/숫자/특수문자 중 2종류 이상을 포함해야 합니다."),
    // ID 존재 여부 / 비밀번호 오류 / 자격증명 미등록 등을 외부에 구분 노출하지 않기 위한 단일 에러
    INVALID_LOGIN_CREDENTIAL(HttpStatus.UNAUTHORIZED, "AUTHENTICATION-0022",
        "로그인 ID 또는 비밀번호가 올바르지 않습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
