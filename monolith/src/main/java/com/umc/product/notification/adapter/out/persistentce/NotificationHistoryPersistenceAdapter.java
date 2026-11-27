package com.umc.product.notification.adapter.out.persistentce;

import java.time.ZoneOffset;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.umc.product.notification.application.port.out.SaveNotificationHistoryPort;
import com.umc.product.notification.domain.NotificationHistory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class NotificationHistoryPersistenceAdapter implements SaveNotificationHistoryPort {

    private static final int BATCH_SIZE = 500;

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void saveIfAbsent(NotificationHistory history, List<Long> memberIds) {
        jdbcTemplate.batchUpdate("""
            INSERT INTO notification_history
                (request_id, member_id, title, body, image_url, deep_link, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (request_id, member_id) DO NOTHING
            """, memberIds, BATCH_SIZE, (statement, memberId) -> {
                statement.setObject(1, history.requestId());
                statement.setLong(2, memberId);
                statement.setString(3, history.title());
                statement.setString(4, history.body());
                statement.setString(5, history.imageUrl());
                statement.setString(6, history.deepLink());
                statement.setObject(7, history.createdAt().atOffset(ZoneOffset.UTC));
            });
    }
}
