package com.umc.product.notification.application.port.in.dto;

import lombok.Builder;

@Builder
public record SendVerificationEmailCommand(
        String to,
        String verificationCode,
        String verificationLink // 추후 인증을 위해서 추가
) {
}
