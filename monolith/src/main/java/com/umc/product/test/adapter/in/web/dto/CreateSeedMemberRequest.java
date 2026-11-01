package com.umc.product.test.adapter.in.web.dto;

import com.umc.product.test.application.port.in.command.dto.CreateSeedMemberCommand;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateSeedMemberRequest(
    @NotBlank(message = "이름은 필수입니다") @Size(max = 30, message = "이름은 30자 이하여야 합니다") String name,

    @NotBlank(message = "닉네임은 필수입니다") @Size(max = 20, message = "닉네임은 20자 이하여야 합니다") String nickname,

    @NotNull(message = "학교 ID는 필수입니다") Long schoolId,

    @NotBlank(message = "이메일은 필수입니다") @Email(message = "이메일 형식이 올바르지 않습니다") @Size(max = 100, message = "이메일은 100자 이하여야 합니다") String email,

    @Size(max = 100, message = "비밀번호는 100자 이하여야 합니다") String rawPassword
) {

    public CreateSeedMemberCommand toCommand() {
        return CreateSeedMemberCommand.of(name, nickname, schoolId, email, rawPassword);
    }
}
