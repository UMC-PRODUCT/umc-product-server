package com.umc.product.notification.application.port.in.command;

import java.util.List;

import com.umc.product.notification.domain.NotificationHistory;

public interface RecordNotificationHistoryUseCase {

    void recordHistory(NotificationHistory history, List<Long> memberIds);
}
