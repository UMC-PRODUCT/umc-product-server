package com.umc.product.fcm.adapter.in.web.dto.request;

import jakarta.validation.constraints.NotBlank;

public record   FcmRegistrationRequest(
        @NotBlank String fcmToken
) {
}
