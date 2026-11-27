package com.umc.product.notification.application.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.member.application.port.in.query.CheckMemberExistenceUseCase;
import com.umc.product.notification.application.port.in.query.ListMyNotificationsUseCase;
import com.umc.product.notification.application.port.in.query.dto.NotificationHistoryItemInfo;
import com.umc.product.notification.application.port.in.query.dto.NotificationHistoryPageInfo;
import com.umc.product.notification.application.port.out.LoadNotificationHistoryPort;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NotificationHistoryQueryService implements ListMyNotificationsUseCase {

    private final CheckMemberExistenceUseCase checkMemberExistenceUseCase;
    private final LoadNotificationHistoryPort loadNotificationHistoryPort;

    @Override
    public NotificationHistoryPageInfo listByMemberId(Long memberId, Long cursor, int size) {
        if (!checkMemberExistenceUseCase.existsById(memberId)) {
            throw new AccessDeniedException("존재하지 않는 회원은 알림 이력을 조회할 수 없습니다.");
        }
        List<NotificationHistoryItemInfo> rows = loadNotificationHistoryPort.listByMemberId(memberId, cursor, size + 1);
        boolean hasNext = rows.size() > size;
        List<NotificationHistoryItemInfo> content = hasNext ? List.copyOf(rows.subList(0, size)) : rows;
        Long nextCursor = hasNext ? content.getLast().notificationId() : null;
        return new NotificationHistoryPageInfo(content, nextCursor, hasNext);
    }
}
