package com.umc.product.authentication.adapter.in.web.dto.request;

public record CompleteEmailVerificationRequest(
    Long emailVerificationId,
    String verificationCode
) {
}
