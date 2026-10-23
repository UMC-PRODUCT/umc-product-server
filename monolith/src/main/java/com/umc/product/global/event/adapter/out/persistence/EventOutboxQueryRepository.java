package com.umc.product.global.event.adapter.out.persistence;

import java.time.Instant;
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
              AND processing_group = :processingGroup
            ORDER BY next_attempt_at, id
            FOR UPDATE SKIP LOCKED
            LIMIT :limit
            """, EventOutbox.class);
        query.setParameter("now", now);
        query.setParameter("limit", limit);
        query.setParameter("processingGroup", selection.processingGroup());
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
            WHERE status IN ('PENDING', 'PROCESSING') AND processing_group = :processingGroup
            """);
        query.setParameter("processingGroup", selection.processingGroup());
        Object[] row = (Object[])query.getSingleResult();
        return new OutboxBacklog(((Number)row[0]).longValue(), ((Number)row[1]).longValue(),
            ((Number)row[2]).doubleValue());
    }

}
