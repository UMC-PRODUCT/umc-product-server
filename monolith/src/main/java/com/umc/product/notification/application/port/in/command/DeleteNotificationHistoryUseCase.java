package com.umc.product.notification.application.port.in.command;

public interface DeleteNotificationHistoryUseCase {

    void deleteByRecipientMemberId(Long memberId);
}
