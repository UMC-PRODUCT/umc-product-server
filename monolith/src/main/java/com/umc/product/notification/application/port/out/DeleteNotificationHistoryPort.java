package com.umc.product.notification.application.port.out;

public interface DeleteNotificationHistoryPort {

    void deleteByRecipientMemberId(Long memberId);
}
