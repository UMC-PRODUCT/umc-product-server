package com.umc.product.notification.application.service;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.notification.application.event.FcmNotificationRequestedEvent;
import com.umc.product.notification.application.port.out.SaveNotificationHistoryPort;
import com.umc.product.notification.domain.NotificationHistory;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationHistoryCommandService {

    private final GetMemberUseCase getMemberUseCase;
    private final SaveNotificationHistoryPort saveNotificationHistoryPort;

    public void save(FcmNotificationRequestedEvent event, List<Long> memberIds) {
        Set<Long> existingMemberIds = getMemberUseCase.findAllBasicByIds(Set.copyOf(memberIds)).keySet();
        List<Long> recipients = memberIds.stream().filter(existingMemberIds::contains).toList();
        if (recipients.isEmpty()) {
            return;
        }

        NotificationHistory history = new NotificationHistory(
            event.requestId(), event.title(), event.body(),
            event.imageUrl(), event.deepLink(), event.occurredAt()
        );
        saveNotificationHistoryPort.saveIfAbsent(history, recipients);
    }
}
