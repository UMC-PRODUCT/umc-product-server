package com.umc.product.authentication.application.port.in.command.dto;

import com.umc.product.authentication.domain.CredentialPolicy;

/**
 * 이메일 기반 자격증명 최초 등록 커맨드. ADR-017 흐름에서 사용한다.
 * <p>
 * email 은 회원 생성 단계에서 emailVerificationToken 으로 이미 검증되어
 * Member.email 에 저장되어 있으므로, 본 커맨드는 비밀번호만 받는다.
 * <p>
 * 비밀번호는 평문으로 들어오며, 인코딩은 Auth Service 에서 수행한다.
 */
public record RegisterCredentialByEmailCommand(
    Long memberId,
    String rawPassword
) {
    public RegisterCredentialByEmailCommand {
        CredentialPolicy.validatePassword(rawPassword);
    }

    public static RegisterCredentialByEmailCommand of(Long memberId, String rawPassword) {
        return new RegisterCredentialByEmailCommand(memberId, rawPassword);
    }

    @Override
    public String toString() {
        return "RegisterCredentialByEmailCommand[memberId=" + memberId + ", rawPassword=***]";
    }
}
