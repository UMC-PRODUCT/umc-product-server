package com.umc.product.notification.application.port.out;

import java.util.List;

import com.umc.product.notification.domain.NotificationHistory;

public interface SaveNotificationHistoryPort {

    void saveIfAbsent(NotificationHistory history, List<Long> memberIds);
}
