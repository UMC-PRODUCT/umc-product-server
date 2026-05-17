package com.umc.product.authentication.application.port.in.command.dto;

/**
 * 이메일/PW 로그인 커맨드.
 * <p>
 * 형식 검증은 일부러 수행하지 않는다. 로그인 단계에서는 "이메일 형식 오류" 와 "이메일/PW 불일치" 를
 * 외부에 구분 노출하지 않기 위함이다 (사용자 열거 / 형식 추측 공격 방어).
 */
public record LoginByEmailCommand(
    String email,
    String rawPassword
) {
    public static LoginByEmailCommand of(String email, String rawPassword) {
        return new LoginByEmailCommand(email, rawPassword);
    }

    @Override
    public String toString() {
        return "LoginByEmailCommand[email=" + email + ", rawPassword=***]";
    }
}
