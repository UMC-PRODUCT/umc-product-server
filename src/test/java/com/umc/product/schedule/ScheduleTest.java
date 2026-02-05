package com.umc.product.schedule;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.umc.product.schedule.domain.Schedule;
import com.umc.product.schedule.domain.enums.ScheduleTag;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled("LocalDateTime -> Instant로 변경함에 따라 비활성화 처리")
class ScheduleTest {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Test
    void 진행중인_일정인지_확인한다() {
        // given
        Instant start = LocalDateTime.of(2024, 3, 16, 10, 0).atZone(KST).toInstant();
        Instant end = LocalDateTime.of(2024, 3, 16, 12, 0).atZone(KST).toInstant();

        Schedule schedule = Schedule.builder()
            .name("테스트")
            .startsAt(start)
            .endsAt(end)
            .tags(Set.of(ScheduleTag.PROJECT))
            .authorChallengerId(1L)
            .build();

        // when
        Instant now = LocalDateTime.of(2024, 3, 16, 11, 0).atZone(KST).toInstant();
        boolean result = schedule.isInProgress(now);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void 시작_전이면_진행중이_아니다() {
        // given
        Instant start = LocalDateTime.of(2024, 3, 16, 10, 0).atZone(KST).toInstant();
        Instant end = LocalDateTime.of(2024, 3, 16, 12, 0).atZone(KST).toInstant();

        Schedule schedule = Schedule.builder()
            .name("테스트")
            .startsAt(start)
            .endsAt(end)
            .tags(Set.of(ScheduleTag.PROJECT))
            .authorChallengerId(1L)
            .build();

        // when
        Instant now = LocalDateTime.of(2024, 3, 16, 9, 0).atZone(KST).toInstant();
        boolean result = schedule.isInProgress(now);

        // then
        assertThat(result).isFalse();
    }
}
