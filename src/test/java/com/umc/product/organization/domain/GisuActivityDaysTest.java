package com.umc.product.organization.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Gisu.activityDays 도메인 메서드")
class GisuActivityDaysTest {

    private static final Instant START = Instant.parse("2026-03-01T00:00:00Z");
    private static final Instant END = Instant.parse("2026-09-01T00:00:00Z");

    private Gisu createGisu() {
        return Gisu.create(7L, START, END, true);
    }

    @Test
    void now가_시작일_이전이면_0일을_반환한다() {
        Gisu gisu = createGisu();
        Instant before = START.minus(10, ChronoUnit.DAYS);

        long days = gisu.activityDays(before);

        assertThat(days).isZero();
    }

    @Test
    void now가_시작일_직전_경계이면_0일을_반환한다() {
        Gisu gisu = createGisu();

        long days = gisu.activityDays(START.minusNanos(1));

        assertThat(days).isZero();
    }

    @Test
    void now가_시작일_정각이면_0일을_반환한다() {
        Gisu gisu = createGisu();

        long days = gisu.activityDays(START);

        assertThat(days).isZero();
    }

    @Test
    void 진행중인_기수는_now까지의_일수를_반환한다() {
        Gisu gisu = createGisu();
        Instant midway = START.plus(45, ChronoUnit.DAYS);

        long days = gisu.activityDays(midway);

        assertThat(days).isEqualTo(45L);
    }

    @Test
    void 종료된_기수는_전체_기간을_반환한다() {
        Gisu gisu = createGisu();
        Instant afterEnd = END.plus(30, ChronoUnit.DAYS);

        long days = gisu.activityDays(afterEnd);

        long expected = ChronoUnit.DAYS.between(START, END);
        assertThat(days).isEqualTo(expected);
    }

    @Test
    void now가_정확히_종료일이면_전체_기간을_반환한다() {
        Gisu gisu = createGisu();

        long days = gisu.activityDays(END);

        long expected = ChronoUnit.DAYS.between(START, END);
        assertThat(days).isEqualTo(expected);
    }
}
