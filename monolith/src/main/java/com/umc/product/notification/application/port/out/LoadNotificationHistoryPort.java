package com.umc.product.notification.application.port.out;

import java.util.List;

import com.umc.product.notification.application.port.in.query.dto.NotificationHistoryItemInfo;

public interface LoadNotificationHistoryPort {

    List<NotificationHistoryItemInfo> listByMemberId(Long memberId, Long cursor, int limit);
}
