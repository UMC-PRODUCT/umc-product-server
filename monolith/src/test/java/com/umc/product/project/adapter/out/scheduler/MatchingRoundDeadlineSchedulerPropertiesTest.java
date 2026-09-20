package com.umc.product.project.adapter.out.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MatchingRoundDeadlineSchedulerPropertiesTest {

    @Test
    @DisplayName("deadline buffer 설정이 없으면 기본값 1분을 사용한다")
    void deadline_buffer_설정이_없으면_기본값_1분을_사용한다() {
        MatchingRoundDeadlineSchedulerProperties properties =
            new MatchingRoundDeadlineSchedulerProperties(null);

        assertThat(properties.deadlineBuffer()).isEqualTo(Duration.ofMinutes(1));
    }

    @Test
    @DisplayName("deadline buffer 설정은 분 단위 Duration으로 변환한다")
    void deadline_buffer_설정은_분_단위_Duration으로_변환한다() {
        MatchingRoundDeadlineSchedulerProperties properties =
            new MatchingRoundDeadlineSchedulerProperties(5L);

        assertThat(properties.deadlineBuffer()).isEqualTo(Duration.ofMinutes(5));
    }

    @Test
    @DisplayName("deadline buffer 설정은 1분 이상이어야 한다")
    void deadline_buffer_설정은_1분_이상이어야_한다() {
        assertThatThrownBy(() -> new MatchingRoundDeadlineSchedulerProperties(0L))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
