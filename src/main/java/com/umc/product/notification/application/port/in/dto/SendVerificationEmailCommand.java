package com.umc.product.notification.application.port.in.dto;

import lombok.Builder;

@Builder
public record SendVerificationEmailCommand(
        String to,
        String verificationToken
) {
}
