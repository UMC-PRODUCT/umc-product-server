package com.umc.product.authentication.adapter.in.web.dto.request;

import com.umc.product.authentication.application.port.in.command.dto.ChangePasswordCommand;
import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(
    @NotBlank String currentPassword,
    @NotBlank String newPassword
) {
    public ChangePasswordCommand toCommand(Long memberId) {
        return ChangePasswordCommand.of(memberId, this.currentPassword, this.newPassword);
    }

    @Override
    public String toString() {
        return "ChangePasswordRequest[currentPassword=***, newPassword=***]";
    }
}
