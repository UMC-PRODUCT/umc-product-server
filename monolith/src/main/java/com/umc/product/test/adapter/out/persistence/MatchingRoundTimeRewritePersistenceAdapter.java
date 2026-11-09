package com.umc.product.test.adapter.out.persistence;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

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
                SELECT r.type, r.chapter_id, %s
                FROM project_matching_round r
                JOIN project_matching_round target
                    ON target.chapter_id = r.chapter_id AND target.type = r.type
                WHERE target.id = :roundId
                ORDER BY r.id
                FOR UPDATE OF r
                """.formatted(MatchingRoundRows.TIME_COLUMNS))
            .setParameter("roundId", roundId)
            .getResultList();
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        Object[] first = rows.getFirst();
        List<RoundTimes> rounds = rows.stream().map(row -> MatchingRoundRows.roundTimes(row, 2)).toList();
        return Optional.of(new MatchingSequence(
            MatchingType.valueOf((String) first[0]), MatchingRoundRows.toLong(first[1]), rounds));
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
}
