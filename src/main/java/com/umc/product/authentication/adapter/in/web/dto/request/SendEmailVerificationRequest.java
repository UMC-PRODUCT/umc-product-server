package com.umc.product.authentication.adapter.in.web.dto.request;

import com.umc.product.authentication.domain.EmailVerificationPurpose;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SendEmailVerificationRequest(
    @NotBlank @Email @Size(max = 100) String email,

    /**
     * 인증 세션 용도. 회원가입(REGISTER) / 비밀번호 초기화(PASSWORD_RESET) / 이메일 변경(CHANGE_EMAIL).
     * cross-purpose 공격 방어를 위해 세션 단위로 고정한다.
     */
    @NotNull EmailVerificationPurpose purpose
) {
}
