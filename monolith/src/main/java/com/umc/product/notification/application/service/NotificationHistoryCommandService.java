package com.umc.product.notification.application.service;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.member.application.port.in.command.LockMemberUseCase;
import com.umc.product.notification.application.port.in.command.DeleteNotificationHistoryUseCase;
import com.umc.product.notification.application.port.in.command.RecordNotificationHistoryUseCase;
import com.umc.product.notification.application.port.out.DeleteNotificationHistoryPort;
import com.umc.product.notification.application.port.out.SaveNotificationHistoryPort;
import com.umc.product.notification.domain.NotificationHistory;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationHistoryCommandService implements RecordNotificationHistoryUseCase,
    DeleteNotificationHistoryUseCase {

    private final LockMemberUseCase lockMemberUseCase;
    private final SaveNotificationHistoryPort saveNotificationHistoryPort;
    private final DeleteNotificationHistoryPort deleteNotificationHistoryPort;

    @Override
    public void recordHistory(NotificationHistory history, List<Long> memberIds) {
        List<Long> recipients = lockMemberUseCase.lockExistingIdsForShare(Set.copyOf(memberIds));
        if (recipients.isEmpty()) {
            return;
        }

        saveNotificationHistoryPort.saveIfAbsent(history, recipients);
    }

    @Override
    public void deleteByRecipientMemberId(Long memberId) {
        deleteNotificationHistoryPort.deleteByRecipientMemberId(memberId);
    }
}
