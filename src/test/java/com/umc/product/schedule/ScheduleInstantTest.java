package com.umc.product.schedule;

import static com.umc.product.schedule.domain.ScheduleConstants.KST;
import static org.assertj.core.api.Assertions.assertThat;

import com.umc.product.schedule.domain.Schedule;
import com.umc.product.schedule.domain.enums.ScheduleTag;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ScheduleInstantTest {

    @Test
    @DisplayName("KST 14시는 UTC로 변환했을 때 9시간 느린 05시여야 한다 (Instant 기본 동작 확인)")
    void Instant_타임존_변환_확인() {
        // Given: 한국 시간 2026-02-07 14:00:00 (UTC 05:00)
        Instant kst14 = Instant.parse("2026-02-07T05:00:00Z");

        // When: KST로 변환
        ZonedDateTime zdt = kst14.atZone(KST);
        int hour = zdt.getHour();

        // Then: 14시여야 함
        assertThat(hour).isEqualTo(14);
    }

    @Test
    @DisplayName("isAllDay가 true일 때, KST 기준 00:00~23:59 범위로 보정된다")
    void 종일_일정_보정_로직_검증() {
        // Given: 한국 시간 2월 7일 오후 2시 (UTC 05:00:00)
        Instant inputTime = Instant.parse("2026-02-07T05:00:00Z");

        // When: 종일 일정 생성
        Schedule schedule = Schedule.builder()
            .name("종일 일정")
            .startsAt(inputTime)
            .endsAt(inputTime)
            .isAllDay(true)
            .tags(Set.of(ScheduleTag.GENERAL))
            .authorChallengerId(1L)
            .build();

        // 디버깅을 위해 실제 보정된 결과 출력
        ZonedDateTime startKST = schedule.getStartsAt().atZone(KST);
        ZonedDateTime endKST = schedule.getEndsAt().atZone(KST);

        System.out.println("DEBUG - Input UTC: " + inputTime);
        System.out.println("DEBUG - Result Start KST: " + startKST);
        System.out.println("DEBUG - Result End KST: " + endKST);

        // Then: 시작 시각 검증 (KST 00:00:00)
        assertThat(startKST.getHour())
            .as("시작 시간의 '시'가 0이어야 함 (실제값: %d)", startKST.getHour())
            .isEqualTo(0);
        assertThat(startKST.getMinute()).isEqualTo(0);
        assertThat(startKST.getSecond()).isEqualTo(0);

        // Then: 종료 시각 검증 (KST 23:59:59)
        assertThat(endKST.getHour())
            .as("종료 시간의 '시'가 23이어야 함 (실제값: %d)", endKST.getHour())
            .isEqualTo(23);
        assertThat(endKST.getMinute()).isEqualTo(59);
        assertThat(endKST.getSecond()).isEqualTo(59);
    }

    @Test
    @DisplayName("Instant 기반의 상태 판별 로직이 정확한지 확인한다")
    void 일정_상태_판별_검증() {
        // Given: UTC 10:00 ~ 12:00 일정
        Instant start = Instant.parse("2026-02-07T10:00:00Z");
        Instant end = Instant.parse("2026-02-07T12:00:00Z");

        Schedule schedule = Schedule.builder()
            .name("상태 테스트")
            .startsAt(start)
            .endsAt(end)
            .tags(Set.of(ScheduleTag.GENERAL))
            .authorChallengerId(1L)
            .build();

        // When & Then
        // 1. 시작 1초 전: 예정
        assertThat(schedule.resolveStatus(start.minusSeconds(1))).isEqualTo("예정");
        // 2. 시작 시점: 진행 중 (isInProgress 로직 확인)
        assertThat(schedule.resolveStatus(start.plusSeconds(1))).isEqualTo("진행 중");
        // 3. 종료 이후: 종료됨
        assertThat(schedule.resolveStatus(end.plusSeconds(1))).isEqualTo("종료됨");
    }
}
