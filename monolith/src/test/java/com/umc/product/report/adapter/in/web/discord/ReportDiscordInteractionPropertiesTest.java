package com.umc.product.report.adapter.in.web.discord;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Discord 상호작용 설정")
class ReportDiscordInteractionPropertiesTest {

    @Test
    @DisplayName("단일 역할 ID도 그대로 읽는다")
    void 단일_값() {
        assertThat(properties("200").moderatorRoleIds()).containsExactlyInAnyOrder("200");
    }

    @Test
    @DisplayName("쉼표로 나누고 공백과 빈 항목은 버린다")
    void 공백과_빈_항목() {
        assertThat(properties("111, 222 ,,333").moderatorRoleIds()).containsExactlyInAnyOrder("111", "222", "333");
    }

    @Test
    @DisplayName("빈 문자열이나 null이면 역할이 없다")
    void 빈_문자열과_null() {
        assertThat(properties("").moderatorRoleIds()).isEmpty();
        assertThat(properties("  ").moderatorRoleIds()).isEmpty();
        assertThat(properties(" , ,").moderatorRoleIds()).isEmpty();
        assertThat(properties(null).moderatorRoleIds()).isEmpty();
    }

    @Test
    @DisplayName("서버 ID가 있고 역할이 하나 이상일 때만 처리 범위가 있다")
    void 처리_범위() {
        assertThat(properties("111,222").hasModeratorScope()).isTrue();
        assertThat(properties(" , ").hasModeratorScope()).isFalse();
        assertThat(properties(null).hasModeratorScope()).isFalse();
        assertThat(new ReportDiscordInteractionProperties("key", null, "111").hasModeratorScope()).isFalse();
        assertThat(new ReportDiscordInteractionProperties("key", " ", "111").hasModeratorScope()).isFalse();
    }

    private static ReportDiscordInteractionProperties properties(String moderatorRoleId) {
        return new ReportDiscordInteractionProperties("key", "100", moderatorRoleId);
    }
}
