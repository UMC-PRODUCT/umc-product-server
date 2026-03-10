package com.umc.product.test.dto;

import com.umc.product.notification.application.port.in.dto.NotificationCommand;

public record FcmTestSendRequest(Long memberId, String title, String body) {

    public NotificationCommand toCommand() {
        return new NotificationCommand(
                memberId,
                title,
                body
        );
    }
}
