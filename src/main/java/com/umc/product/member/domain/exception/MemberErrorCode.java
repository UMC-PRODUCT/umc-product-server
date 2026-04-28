package com.umc.product.member.domain.exception;

import com.umc.product.global.response.code.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MemberErrorCode implements BaseCode {

    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER-0001", "사용자를 찾을 수 없습니다."),
    MEMBER_ALREADY_EXISTS(HttpStatus.CONFLICT, "MEMBER-0002", "이미 등록된 사용자입니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "MEMBER-0003", "이미 사용 중인 이메일입니다."),
    MEMBER_ALREADY_WITHDRAWN(HttpStatus.BAD_REQUEST, "MEMBER-0004", "이미 탈퇴한 사용자입니다."),
    INVALID_MEMBER_STATUS(HttpStatus.BAD_REQUEST, "MEMBER-0005", "올바르지 않은 사용자 상태입니다."),
    MEMBER_NOT_ACTIVE(HttpStatus.BAD_REQUEST, "MEMBER-0006", "올바르지 않은 사용자 상태입니다."),
    MEMBER_ALREADY_REGISTERED(HttpStatus.BAD_REQUEST, "MEMBER-0007", "이미 회원가입을 완료한 사용자입니다."),
    MEMBER_PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER-0008", "프로필을 찾을 수 없습니다."),
    MEMBER_SCHOOL_NOT_ASSIGNED(HttpStatus.BAD_REQUEST, "MEMBER-0009", "학교가 등록되지 않은 사용자입니다."),
    CREDENTIAL_ALREADY_REGISTERED(HttpStatus.CONFLICT, "MEMBER-0010", "이미 ID/PW 자격증명이 등록된 사용자입니다."),
    CREDENTIAL_NOT_REGISTERED(HttpStatus.BAD_REQUEST, "MEMBER-0011", "ID/PW 자격증명이 등록되지 않은 사용자입니다."),
    INVALID_LOGIN_ID(HttpStatus.BAD_REQUEST, "MEMBER-0012", "올바르지 않은 로그인 아이디입니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "MEMBER-0013", "올바르지 않은 비밀번호입니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
