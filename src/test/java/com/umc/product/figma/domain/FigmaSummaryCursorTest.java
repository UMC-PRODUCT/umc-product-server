package com.umc.product.figma.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("FigmaSummaryCursor")
class FigmaSummaryCursorTest {

    @Test
    @DisplayName("bootstrap 한 cursor 의 lastWindowEnd 가 입력 시각과 일치한다")
    void bootstrap_입력_시각으로_초기화() {
        Instant initial = Instant.parse("2026-05-07T10:00:00Z");
        FigmaSummaryCursor cursor = FigmaSummaryCursor.bootstrap(initial);
        assertThat(cursor.getLastWindowEnd()).isEqualTo(initial);
    }

    @Test
    @DisplayName("advance 가 미래 시각이면 lastWindowEnd 가 갱신된다")
    void advance_미래_시각_갱신() {
        FigmaSummaryCursor cursor = FigmaSummaryCursor.bootstrap(Instant.parse("2026-05-07T10:00:00Z"));
        Instant later = Instant.parse("2026-05-07T10:05:00Z");

        cursor.advance(later);

        assertThat(cursor.getLastWindowEnd()).isEqualTo(later);
    }

    @Test
    @DisplayName("advance 가 과거 시각이면 거절되고 lastWindowEnd 는 그대로다 — 같은 시간창 재발송 방지")
    void advance_과거_시각_거절() {
        Instant initial = Instant.parse("2026-05-07T10:00:00Z");
        FigmaSummaryCursor cursor = FigmaSummaryCursor.bootstrap(initial);
        Instant earlier = Instant.parse("2026-05-07T09:55:00Z");

        cursor.advance(earlier);

        assertThat(cursor.getLastWindowEnd()).isEqualTo(initial);
    }

    @Test
    @DisplayName("advance 가 같은 시각이면 idempotent 하게 그대로 둔다")
    void advance_동일_시각_idempotent() {
        Instant initial = Instant.parse("2026-05-07T10:00:00Z");
        FigmaSummaryCursor cursor = FigmaSummaryCursor.bootstrap(initial);

        cursor.advance(initial);

        assertThat(cursor.getLastWindowEnd()).isEqualTo(initial);
    }

    @Test
    @DisplayName("advance 에 null 을 넘기면 lastWindowEnd 가 변경되지 않는다")
    void advance_null_무시() {
        Instant initial = Instant.parse("2026-05-07T10:00:00Z");
        FigmaSummaryCursor cursor = FigmaSummaryCursor.bootstrap(initial);

        cursor.advance(null);

        assertThat(cursor.getLastWindowEnd()).isEqualTo(initial);
    }
}
