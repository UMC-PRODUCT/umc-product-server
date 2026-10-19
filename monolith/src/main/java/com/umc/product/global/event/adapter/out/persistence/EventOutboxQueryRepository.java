package com.umc.product.global.event.adapter.out.persistence;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.umc.product.global.event.application.port.out.OutboxEventSelection;
import com.umc.product.global.event.domain.EventOutbox;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class EventOutboxQueryRepository {

    private final EntityManager entityManager;

    public List<EventOutbox> findPublishableForUpdate(OutboxEventSelection selection, int limit, Instant now) {
        if (limit <= 0) {
            throw new IllegalArgumentException("배치 크기는 양수여야 합니다.");
        }
        List<String> conditions = new ArrayList<>();
        if (!selection.includedTypes().isEmpty()) {
            conditions.add("event_type IN (:includedTypes)");
        }
        String fallback = switch (selection.prefixMatch()) {
            case MATCH -> "event_type LIKE :prefixPattern";
            case NOT_MATCH -> "event_type NOT LIKE :prefixPattern";
            case NONE -> null;
        };
        if (fallback != null) {
            if (!selection.overriddenTypes().isEmpty()) {
                fallback += " AND event_type NOT IN (:overriddenTypes)";
            }
            conditions.add("(" + fallback + ")");
        }
        if (conditions.isEmpty()) {
            return List.of();
        }
        Query query = entityManager.createNativeQuery("""
            SELECT * FROM event_outbox
            WHERE status IN ('PENDING', 'PROCESSING')
              AND next_attempt_at <= :now
              AND (%s)
            ORDER BY next_attempt_at, id
            FOR UPDATE SKIP LOCKED
            LIMIT :limit
            """.formatted(String.join(" OR ", conditions)), EventOutbox.class);
        query.setParameter("now", now);
        query.setParameter("limit", limit);
        if (!selection.includedTypes().isEmpty()) {
            query.setParameter("includedTypes", selection.includedTypes());
        }
        if (fallback != null) {
            query.setParameter("prefixPattern", selection.prefix() + "%");
        }
        if (fallback != null && !selection.overriddenTypes().isEmpty()) {
            query.setParameter("overriddenTypes", selection.overriddenTypes());
        }
        List<?> results = query.getResultList();
        return results.stream().map(EventOutbox.class::cast).toList();
    }
}
