package com.umc.product.notification.application.port.in.query.dto;

import java.time.Instant;

public record NotificationHistoryItemInfo(
    Long notificationId,
    String title,
    String body,
    String imageUrl,
    String deepLink,
    Instant createdAt
) {
}
