package com.umc.product.notification.application.port.in.dto;

import com.umc.product.notification.domain.WebhookPlatform;
import java.util.List;
import java.util.Objects;
import lombok.Builder;

@Builder
public record SendWebhookAlarmCommand(
    List<WebhookPlatform> platforms,
    String title,
    String content
) {
    public SendWebhookAlarmCommand {
        Objects.requireNonNull(platforms, "platforms must not be null");
        Objects.requireNonNull(title, "title must not be null");
        Objects.requireNonNull(content, "content must not be null");
        if (platforms.isEmpty()) {
            throw new IllegalArgumentException("platforms must not be empty");
        }
    }
}
