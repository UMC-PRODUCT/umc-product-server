package com.umc.product.notification.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WebhookPlatform {
    DISCORD("Discord");

    private final String displayName;
}
