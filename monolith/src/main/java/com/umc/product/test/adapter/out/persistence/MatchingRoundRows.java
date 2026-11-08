package com.umc.product.test.adapter.out.persistence;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;

import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.test.domain.rehearsal.RoundTimes;

final class MatchingRoundRows {

    /** {@code project_matching_round r}의 열. {@link #roundTimes(Object[], int)}가 이 순서로 읽는다. */
    static final String TIME_COLUMNS = """
        r.id, r.phase, r.starts_at, r.ends_at, r.decision_deadline, r.result_publish_at,
        r.auto_decision_executed_at, r.result_published_at""";

    private MatchingRoundRows() {
    }

    static RoundTimes roundTimes(Object[] row, int offset) {
        return new RoundTimes(toLong(row[offset]), MatchingPhase.valueOf((String) row[offset + 1]),
            toInstant(row[offset + 2]), toInstant(row[offset + 3]), toInstant(row[offset + 4]),
            toInstant(row[offset + 5]), toInstant(row[offset + 6]), toInstant(row[offset + 7]));
    }

    static Long toLong(Object value) {
        return value == null ? null : ((Number) value).longValue();
    }

    static int toInt(Object value) {
        return ((Number) value).intValue();
    }

    static Instant toInstant(Object value) {
        return switch (value) {
            case null -> null;
            case Instant instant -> instant;
            case Timestamp timestamp -> timestamp.toInstant();
            case OffsetDateTime offsetDateTime -> offsetDateTime.toInstant();
            default -> throw new IllegalStateException("시각 컬럼 타입을 알 수 없습니다: " + value.getClass());
        };
    }
}
