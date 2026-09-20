package com.umc.product.member.adapter.in.web.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ChangeMemberEmailRequest(
    @NotBlank String emailVerificationToken
) {
}
