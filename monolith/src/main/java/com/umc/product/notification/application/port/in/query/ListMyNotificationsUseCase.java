package com.umc.product.notification.application.port.in.query;

import com.umc.product.notification.application.port.in.query.dto.NotificationHistoryPageInfo;

public interface ListMyNotificationsUseCase {

    NotificationHistoryPageInfo listByMemberId(Long memberId, Long cursor, int size);
}
