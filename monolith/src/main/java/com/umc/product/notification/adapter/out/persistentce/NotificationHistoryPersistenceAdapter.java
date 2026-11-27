package com.umc.product.notification.adapter.out.persistentce;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.umc.product.notification.application.port.in.query.dto.NotificationHistoryItemInfo;
import com.umc.product.notification.application.port.out.LoadNotificationHistoryPort;
import com.umc.product.notification.application.port.out.SaveNotificationHistoryPort;
import com.umc.product.notification.domain.NotificationHistory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class NotificationHistoryPersistenceAdapter implements SaveNotificationHistoryPort, LoadNotificationHistoryPort {

    private static final int BATCH_SIZE = 500;
    private static final String SELECT_BY_MEMBER = """
        SELECT id, title, body, image_url, deep_link, created_at
        FROM notification_history
        WHERE member_id = ?
        """;
    private static final String SEEK_AFTER_CURSOR = """
        AND (created_at, id) < (
            SELECT created_at, id FROM notification_history WHERE id = ? AND member_id = ?
        )
        """;
    private static final String PAGE_ORDER = """
        ORDER BY created_at DESC, id DESC
        LIMIT ?
        """;
    private static final RowMapper<NotificationHistoryItemInfo> HISTORY_ITEM_MAPPER = (row, rowNum) ->
        new NotificationHistoryItemInfo(
            row.getLong("id"), row.getString("title"), row.getString("body"),
            row.getString("image_url"), row.getString("deep_link"),
            row.getObject("created_at", OffsetDateTime.class).toInstant()
        );

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<NotificationHistoryItemInfo> listByMemberId(Long memberId, Long cursor, int limit) {
        if (cursor == null) {
            return jdbcTemplate.query(SELECT_BY_MEMBER + PAGE_ORDER, HISTORY_ITEM_MAPPER, memberId, limit);
        }
        return jdbcTemplate.query(
            SELECT_BY_MEMBER + SEEK_AFTER_CURSOR + PAGE_ORDER, HISTORY_ITEM_MAPPER, memberId, cursor, memberId, limit
        );
    }

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
