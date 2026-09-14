package com.umc.product.authentication.adapter.in.web.dto.request;

import com.umc.product.authentication.application.port.in.command.dto.ResetPasswordByEmailCommand;
import jakarta.validation.constraints.NotBlank;

/**
 * 이메일 인증 기반 비밀번호 초기화 요청.
 * <p>
 * 사용자가 별도 인증 없이 emailVerificationToken 으로 신원을 증명한 뒤 새 비밀번호로 교체한다.
 */
public record ResetPasswordByEmailRequest(
    @NotBlank String emailVerificationToken,
    @NotBlank String newPassword
) {
    public ResetPasswordByEmailCommand toCommand(String email) {
        return ResetPasswordByEmailCommand.of(email, this.newPassword);
    }

    @Override
    public String toString() {
        return "ResetPasswordByEmailRequest[emailVerificationToken=***, newPassword=***]";
    }
}
