package com.umc.product.authentication.application.port.in.command.dto;

import com.umc.product.authentication.domain.CredentialPolicy;

/**
 * ID/PW 자격증명 최초 등록 커맨드.
 * <p>
 * 비밀번호는 평문으로 들어오며, 인코딩은 Auth Service 에서 수행한다.
 * Compact constructor 에서 형식/정책을 강제하여 부적합한 입력은 도메인 진입 전에 차단한다.
 */
public record RegisterCredentialCommand(
    Long memberId,
    String loginId,
    String rawPassword
) {
    public RegisterCredentialCommand {
        CredentialPolicy.validateLoginId(loginId);
        CredentialPolicy.validatePassword(rawPassword);
    }

    public static RegisterCredentialCommand of(Long memberId, String loginId, String rawPassword) {
        return new RegisterCredentialCommand(memberId, loginId, rawPassword);
    }

    @Override
    public String toString() {
        // rawPassword 가 우연히 로그에 노출되지 않도록 마스킹한다.
        return "RegisterCredentialCommand[memberId=" + memberId
            + ", loginId=" + loginId + ", rawPassword=***]";
    }
}
