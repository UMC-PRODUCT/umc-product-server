package com.umc.product.authentication.application.port.in.command.dto;

import com.umc.product.authentication.domain.CredentialPolicy;

/**
 * 이메일 인증 기반 비밀번호 초기화 커맨드.
 * <p>
 * 사용자가 비밀번호를 잊었을 때, 이메일 인증으로 신원을 확인한 뒤 새 비밀번호로 교체하는 흐름에서 사용한다.
 * email 은 이미 emailVerificationToken 으로 검증되어 추출된 값이다.
 */
public record ResetPasswordByEmailCommand(
    String email,
    String newRawPassword
) {
    public ResetPasswordByEmailCommand {
        CredentialPolicy.validatePassword(newRawPassword);
    }

    public static ResetPasswordByEmailCommand of(String email, String newRawPassword) {
        return new ResetPasswordByEmailCommand(email, newRawPassword);
    }

    @Override
    public String toString() {
        return "ResetPasswordByEmailCommand[email=" + email + ", newRawPassword=***]";
    }
}
