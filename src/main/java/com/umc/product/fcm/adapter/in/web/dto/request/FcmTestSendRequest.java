package com.umc.product.fcm.adapter.in.web.dto.request;

import com.umc.product.fcm.application.port.in.NotificationCommand;

public record FcmTestSendRequest(Long memberId, String title, String body) {

    public NotificationCommand toCommand() {
        return new NotificationCommand(
                memberId,
                title,
                body
        );
    }
}
