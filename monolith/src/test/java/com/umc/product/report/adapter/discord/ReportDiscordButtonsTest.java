package com.umc.product.report.adapter.discord;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.umc.product.report.domain.enums.ReportActionType;

@DisplayName("신고 조치 버튼 형식")
class ReportDiscordButtonsTest {

    @Test
    @DisplayName("처리 완료와 반려 버튼을 report:{id}:{조치} 형식으로 만든다")
    void actionRows_버튼을_만든다() {
        // when
        List<Map<String, Object>> buttons = buttons(ReportDiscordButtons.actionRows(901L, false));

        // then
        assertThat(buttons).extracting(button -> button.get("label")).containsExactly("처리 완료", "반려");
        assertThat(buttons).extracting(button -> button.get("custom_id"))
            .containsExactly("report:901:approve", "report:901:reject");
        assertThat(buttons).extracting(button -> button.get("disabled")).containsOnly(false);
    }

    @Test
    @DisplayName("처리된 메시지에는 비활성 버튼을 만든다")
    void actionRows_비활성_버튼() {
        List<Map<String, Object>> buttons = buttons(ReportDiscordButtons.actionRows(901L, true));

        assertThat(buttons).extracting(button -> button.get("disabled")).containsOnly(true);
    }

    @Test
    @DisplayName("만든 버튼 ID를 다시 읽을 수 있다")
    void parse_버튼_ID를_읽는다() {
        for (ReportActionType actionType : ReportActionType.values()) {
            String customId = ReportDiscordButtons.customId(901L, actionType);

            assertThat(ReportDiscordButtons.parse(customId))
                .contains(new ReportDiscordButtons.Target(901L, actionType));
        }
    }

    @Test
    @DisplayName("형식이 다른 버튼 ID는 읽지 않는다")
    void parse_형식이_다르면_비어_있다() {
        assertThat(ReportDiscordButtons.parse(null)).isEmpty();
        assertThat(ReportDiscordButtons.parse("report:901")).isEmpty();
        assertThat(ReportDiscordButtons.parse("other:901:approve")).isEmpty();
        assertThat(ReportDiscordButtons.parse("report:abc:approve")).isEmpty();
        assertThat(ReportDiscordButtons.parse("report:0:approve")).isEmpty();
        assertThat(ReportDiscordButtons.parse("report:901:delete")).isEmpty();
        assertThat(ReportDiscordButtons.parse("report:901:approve:extra")).isEmpty();
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> buttons(List<Map<String, Object>> rows) {
        return (List<Map<String, Object>>) rows.get(0).get("components");
    }
}
