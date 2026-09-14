package com.umc.product.notification.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WebhookPlatform {
    SLACK("Slack"),
    DISCORD("Discord"),
    TELEGRAM("Telegram"),
    ;

    private final String displayName;
}
