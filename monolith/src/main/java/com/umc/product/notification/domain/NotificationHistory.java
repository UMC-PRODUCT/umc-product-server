package com.umc.product.notification.domain;

import java.time.Instant;
import java.util.UUID;

public record NotificationHistory(
    UUID requestId,
    String title,
    String body,
    String imageUrl,
    String deepLink,
    Instant createdAt
) {
}
