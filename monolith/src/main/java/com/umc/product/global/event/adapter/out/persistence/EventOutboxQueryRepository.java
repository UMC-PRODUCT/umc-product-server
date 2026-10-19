package com.umc.product.global.event.adapter.out.persistence;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.global.event.application.port.out.LoadOutboxBacklogPort;
import com.umc.product.global.event.application.port.out.LoadOutboxBacklogPort.OutboxBacklog;
import com.umc.product.global.event.application.port.out.OutboxEventSelection;
import com.umc.product.global.event.domain.EventOutbox;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class EventOutboxQueryRepository implements LoadOutboxBacklogPort {

    private final EntityManager entityManager;

    public List<EventOutbox> findPublishableForUpdate(OutboxEventSelection selection, int limit, Instant now) {
        if (limit <= 0) {
            throw new IllegalArgumentException("배치 크기는 양수여야 합니다.");
        }
        Query query = entityManager.createNativeQuery("""
            SELECT * FROM event_outbox
            WHERE status IN ('PENDING', 'PROCESSING')
              AND next_attempt_at <= :now
              AND (%s)
            ORDER BY next_attempt_at, id
            FOR UPDATE SKIP LOCKED
            LIMIT :limit
            """.formatted(selectionPredicate(selection)), EventOutbox.class);
        query.setParameter("now", now);
        query.setParameter("limit", limit);
        bindSelection(query, selection);
        List<?> results = query.getResultList();
        return results.stream().map(EventOutbox.class::cast).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OutboxBacklog getBacklog(OutboxEventSelection selection) {
        Query query = entityManager.createNativeQuery("""
            SELECT COUNT(*) FILTER (WHERE status = 'PENDING'),
                   COUNT(*) FILTER (WHERE status = 'PROCESSING'),
                   COALESCE(GREATEST(EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - MIN(created_at))), 0), 0)
            FROM event_outbox
            WHERE status IN ('PENDING', 'PROCESSING') AND (%s)
            """.formatted(selectionPredicate(selection)));
        bindSelection(query, selection);
        Object[] row = (Object[])query.getSingleResult();
        return new OutboxBacklog(((Number)row[0]).longValue(), ((Number)row[1]).longValue(),
            ((Number)row[2]).doubleValue());
    }

    private String selectionPredicate(OutboxEventSelection selection) {
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
        return conditions.isEmpty() ? "FALSE" : String.join(" OR ", conditions);
    }

    private void bindSelection(Query query, OutboxEventSelection selection) {
        if (!selection.includedTypes().isEmpty()) {
            query.setParameter("includedTypes", selection.includedTypes());
        }
        if (selection.prefixMatch() != OutboxEventSelection.PrefixMatch.NONE) {
            query.setParameter("prefixPattern", selection.prefix() + "%");
        }
        if (selection.prefixMatch() != OutboxEventSelection.PrefixMatch.NONE
            && !selection.overriddenTypes().isEmpty()) {
            query.setParameter("overriddenTypes", selection.overriddenTypes());
        }
    }

}
