package com.umc.product.notification.adapter.in.web.dto.response;

import java.time.Instant;

import com.umc.product.notification.application.port.in.query.dto.NotificationHistoryItemInfo;

public record NotificationHistoryItemResponse(
    Long notificationId,
    String title,
    String body,
    String imageUrl,
    String deepLink,
    Instant createdAt
) {

    public static NotificationHistoryItemResponse from(NotificationHistoryItemInfo info) {
        return new NotificationHistoryItemResponse(
            info.notificationId(), info.title(), info.body(), info.imageUrl(), info.deepLink(), info.createdAt()
        );
    }
}
