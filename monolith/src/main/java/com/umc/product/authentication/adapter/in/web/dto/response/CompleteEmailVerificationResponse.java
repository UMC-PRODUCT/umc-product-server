package com.umc.product.authentication.adapter.in.web.dto.response;

import lombok.Builder;

@Builder
public record CompleteEmailVerificationResponse(
    String emailVerificationToken
) {
}
