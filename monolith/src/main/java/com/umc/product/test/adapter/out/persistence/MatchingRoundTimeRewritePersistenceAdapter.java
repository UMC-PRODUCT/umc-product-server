package com.umc.product.test.adapter.out.persistence;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.test.application.port.out.RewriteMatchingRoundTimesPort;
import com.umc.product.test.domain.rehearsal.MatchingSequence;
import com.umc.product.test.domain.rehearsal.RoundTimes;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Component
@Profile("!prod")
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class MatchingRoundTimeRewritePersistenceAdapter implements RewriteMatchingRoundTimesPort {

    private final EntityManager entityManager;

    @Override
    @SuppressWarnings("unchecked")
    public Optional<MatchingSequence> lockSequence(Long roundId) {
        List<Object[]> rows = entityManager.createNativeQuery("""
                SELECT r.id, r.type, r.phase, r.chapter_id,
                       r.starts_at, r.ends_at, r.decision_deadline, r.result_publish_at,
                       r.auto_decision_executed_at, r.result_published_at
                FROM project_matching_round r
                JOIN project_matching_round target
                    ON target.chapter_id = r.chapter_id AND target.type = r.type
                WHERE target.id = :roundId
                ORDER BY r.id
                FOR UPDATE OF r
                """)
            .setParameter("roundId", roundId)
            .getResultList();
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        Object[] first = rows.getFirst();
        List<RoundTimes> rounds = rows.stream()
            .map(row -> new RoundTimes(
                ((Number) row[0]).longValue(), MatchingPhase.valueOf((String) row[2]),
                instant(row[4]), instant(row[5]), instant(row[6]), instant(row[7]),
                instant(row[8]), instant(row[9])))
            .toList();
        return Optional.of(new MatchingSequence(
            MatchingType.valueOf((String) first[1]), ((Number) first[3]).longValue(), rounds));
    }

    @Override
    public void rewrite(List<RoundTimes> rounds) {
        Instant now = Instant.now();
        for (RoundTimes round : rounds) {
            entityManager.createNativeQuery("""
                    UPDATE project_matching_round
                    SET starts_at = :startsAt, ends_at = :endsAt, decision_deadline = :decisionDeadline,
                        result_publish_at = :resultPublishAt, updated_at = :updatedAt
                    WHERE id = :id
                    """)
                .setParameter("startsAt", round.startsAt())
                .setParameter("endsAt", round.endsAt())
                .setParameter("decisionDeadline", round.decisionDeadline())
                .setParameter("resultPublishAt", round.resultPublishAt())
                .setParameter("updatedAt", now)
                .setParameter("id", round.roundId())
                .executeUpdate();
        }
    }

    private static Instant instant(Object value) {
        return switch (value) {
            case null -> null;
            case Instant instant -> instant;
            case OffsetDateTime offsetDateTime -> offsetDateTime.toInstant();
            case Timestamp timestamp -> timestamp.toInstant();
            default -> throw new IllegalStateException("시각 컬럼 타입을 알 수 없습니다: " + value.getClass());
        };
    }
}
