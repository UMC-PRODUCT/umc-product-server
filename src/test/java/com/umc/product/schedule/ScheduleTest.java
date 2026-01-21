package com.umc.product.schedule;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.umc.product.schedule.domain.Schedule;
import com.umc.product.schedule.domain.enums.ScheduleType;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class ScheduleTest {

    @Test
    void 진행중인_일정인지_확인한다() {
        // given
        Schedule schedule = Schedule.builder()
                .name("테스트")
                .startsAt(LocalDateTime.of(2024, 3, 16, 10, 0))
                .endsAt(LocalDateTime.of(2024, 3, 16, 12, 0))
                .type(ScheduleType.TEAM_ACTIVITY)
                .authorChallengerId(1L)
                .build();

        // when
        boolean result = schedule.isInProgress(LocalDateTime.of(2024, 3, 16, 11, 0));

        // then
        assertThat(result).isTrue();
    }

    @Test
    void 시작_전이면_진행중이_아니다() {
        // given
        Schedule schedule = Schedule.builder()
                .name("테스트")
                .startsAt(LocalDateTime.of(2024, 3, 16, 10, 0))
                .endsAt(LocalDateTime.of(2024, 3, 16, 12, 0))
                .type(ScheduleType.TEAM_ACTIVITY)
                .authorChallengerId(1L)
                .build();

        // when
        boolean result = schedule.isInProgress(LocalDateTime.of(2024, 3, 16, 9, 0));

        // then
        assertThat(result).isFalse();
    }
}
