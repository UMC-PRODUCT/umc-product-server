package com.umc.product.authentication.domain;

import com.umc.product.authentication.domain.exception.AuthenticationDomainException;
import com.umc.product.authentication.domain.exception.AuthenticationErrorCode;
import java.util.regex.Pattern;

/**
 * ID/PW 자격증명의 형식/정책에 대한 단일 진실 원천(SSOT).
 * <p>
 * 로그인 ID 와 비밀번호의 형식/복잡도 정책을 한 곳에 모아 두고 Command DTO 의 검증 / 회원가입 / 비밀번호 변경 등에서 모두 동일한 규칙을 적용한다.
 * <p>
 * 정책 변경 시 본 파일만 수정하면 된다. (+ DB Check 또한 변경해야 한다)
 */
public final class CredentialPolicy {

    /**
     * 영문 소문자, 숫자, 점/밑줄/하이픈 5~20자. DB CHECK 제약과 일치해야 한다.
     *
     * @deprecated ADR-017 에 따라 로그인 식별자를 이메일로 전환한 뒤 제거 예정.
     */
    @Deprecated
    public static final Pattern LOGIN_ID_PATTERN = Pattern.compile("^[a-z0-9._-]{5,20}$");

    /**
     * 이메일 형식 패턴. RFC 5322 의 실용적 부분 집합으로, 일반적인 이메일 입력을 허용한다.
     * <p>
     * Member.email 컬럼이 VARCHAR(100) 이므로 길이 제한을 함께 강제한다.
     */
    public static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    public static final int EMAIL_MAX_LENGTH = 100;

    public static final int PASSWORD_MIN_LENGTH = 8;
    public static final int PASSWORD_MAX_LENGTH = 64;

    private CredentialPolicy() {
    }

    /**
     * @deprecated ADR-017 에 따라 로그인 식별자가 이메일로 전환된 뒤 제거 예정.
     */
    @Deprecated
    public static void validateLoginId(String loginId) {
        if (loginId == null || !LOGIN_ID_PATTERN.matcher(loginId).matches()) {
            throw new AuthenticationDomainException(AuthenticationErrorCode.INVALID_LOGIN_ID_FORMAT);
        }
    }

    /**
     * 이메일 형식과 길이를 검증한다. 회원가입/자격증명 등록 시점의 SSOT 검증 지점이다.
     */
    public static void validateEmail(String email) {
        if (email == null
            || email.length() > EMAIL_MAX_LENGTH
            || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new AuthenticationDomainException(AuthenticationErrorCode.INVALID_EMAIL_FORMAT);
        }
    }

    /**
     * 비밀번호 정책: 길이 8~64자, 영문/숫자/특수문자 중 2종류 이상 포함.
     * <p>
     * 공백/탭/제어문자만으로 이루어진 비밀번호는 거부한다.
     */
    public static void validatePassword(String rawPassword) {
        if (rawPassword == null) {
            throw new AuthenticationDomainException(AuthenticationErrorCode.PASSWORD_POLICY_VIOLATION);
        }
        int length = rawPassword.length();
        if (length < PASSWORD_MIN_LENGTH || length > PASSWORD_MAX_LENGTH) {
            throw new AuthenticationDomainException(AuthenticationErrorCode.PASSWORD_POLICY_VIOLATION);
        }

        boolean hasLetter = false;
        boolean hasDigit = false;
        boolean hasSymbol = false;
        for (int i = 0; i < length; i++) {
            char c = rawPassword.charAt(i);
            if (Character.isWhitespace(c) || Character.isISOControl(c)) {
                throw new AuthenticationDomainException(AuthenticationErrorCode.PASSWORD_POLICY_VIOLATION);
            }
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else {
                hasSymbol = true;
            }
        }

        int categoryCount = (hasLetter ? 1 : 0) + (hasDigit ? 1 : 0) + (hasSymbol ? 1 : 0);
        if (categoryCount < 2) {
            throw new AuthenticationDomainException(AuthenticationErrorCode.PASSWORD_POLICY_VIOLATION);
        }
    }
}
