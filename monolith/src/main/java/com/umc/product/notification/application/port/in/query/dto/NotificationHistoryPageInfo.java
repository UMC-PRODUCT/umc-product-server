package com.umc.product.notification.application.port.in.query.dto;

import java.util.List;

public record NotificationHistoryPageInfo(
    List<NotificationHistoryItemInfo> content,
    Long nextCursor,
    boolean hasNext
) {
}
