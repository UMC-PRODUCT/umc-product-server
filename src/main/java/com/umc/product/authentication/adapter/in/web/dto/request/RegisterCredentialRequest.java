package com.umc.product.authentication.adapter.in.web.dto.request;

import com.umc.product.authentication.application.port.in.command.dto.RegisterCredentialByEmailCommand;
import jakarta.validation.constraints.NotBlank;

/**
 * 이메일 기반 자격증명 최초 등록 요청. ADR-017 흐름.
 * <p>
 * 형식/정책 검증은 Command 레코드의 compact constructor 에서 수행하므로
 * 여기서는 null/공백 가드만 둔다. 이메일은 이미 Member.email 에 저장되어 있으므로 받지 않는다.
 */
public record RegisterCredentialRequest(
    @NotBlank String rawPassword
) {
    public RegisterCredentialByEmailCommand toCommand(Long memberId) {
        return RegisterCredentialByEmailCommand.of(memberId, this.rawPassword);
    }

    @Override
    public String toString() {
        return "RegisterCredentialRequest[rawPassword=***]";
    }
}
