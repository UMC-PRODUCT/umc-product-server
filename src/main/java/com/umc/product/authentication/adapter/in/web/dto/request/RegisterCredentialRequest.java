package com.umc.product.authentication.adapter.in.web.dto.request;

import com.umc.product.authentication.application.port.in.command.dto.RegisterCredentialCommand;
import jakarta.validation.constraints.NotBlank;

/**
 * ID/PW 자격증명 최초 등록 요청.
 * <p>
 * 형식/정책 검증은 Command 레코드의 compact constructor 에서 수행하므로
 * 여기서는 null/공백 가드만 둔다.
 */
public record RegisterCredentialRequest(
    @NotBlank String loginId,
    @NotBlank String password
) {
    public RegisterCredentialCommand toCommand(Long memberId) {
        return RegisterCredentialCommand.of(memberId, this.loginId, this.password);
    }

    @Override
    public String toString() {
        return "RegisterCredentialRequest[loginId=" + loginId + ", password=***]";
    }
}
