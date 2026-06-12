package com.umc.product.project.application.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProjectMatchingRoundPropertiesTest {

    @Test
    @DisplayName("매칭 차수 간격 설정이 없으면 기본값 1분을 사용한다")
    void 매칭_차수_간격_설정이_없으면_기본값_1분을_사용한다() {
        ProjectMatchingRoundProperties properties = new ProjectMatchingRoundProperties(null);

        assertThat(properties.minPhaseInterval()).isEqualTo(Duration.ofMinutes(1));
    }

    @Test
    @DisplayName("매칭 차수 간격 설정은 분 단위 Duration으로 변환한다")
    void 매칭_차수_간격_설정은_분_단위_Duration으로_변환한다() {
        ProjectMatchingRoundProperties properties = new ProjectMatchingRoundProperties(3L);

        assertThat(properties.minPhaseInterval()).isEqualTo(Duration.ofMinutes(3));
    }

    @Test
    @DisplayName("매칭 차수 간격 설정은 1분 이상이어야 한다")
    void 매칭_차수_간격_설정은_1분_이상이어야_한다() {
        assertThatThrownBy(() -> new ProjectMatchingRoundProperties(0L))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
