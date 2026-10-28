package com.umc.product.report.adapter.out.delivery.discord;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.umc.product.report.domain.Report;
import com.umc.product.report.domain.ReportTargetSnapshot;
import com.umc.product.report.domain.enums.ReportReason;

@DisplayName("Discord 신고 메시지")
class DiscordReportMessageFactoryTest {

    private final DiscordReportMessageFactory sut = new DiscordReportMessageFactory();

    @Test
    @DisplayName("사유·대상·위치·작성자와 200자로 자른 내용, 접수 시각을 담는다")
    void create_검토에_필요한_정보를_담는다() {
        // given
        String longContent = "가".repeat(DiscordReportMessageFactory.CONTENT_PREVIEW_MAX_LENGTH + 50);
        Report report = threadMessageReport(ReportTargetSnapshot.of(4L, longContent, "스레드 제목"));

        // when
        List<Map<String, Object>> fields = fields(sut.create(report));

        // then
        assertThat(fields).extracting(field -> field.get("name"))
            .containsExactly("신고 ID", "사유", "대상", "위치", "작성자 member ID", "내용", "접수 시각");
        assertThat(fields).extracting(field -> field.get("value"))
            .contains("901", "욕설/비방", "스레드 메시지 #33 (스레드 #11)", "스레드 제목", "4", "2026-10-26 18:00");
        String content = (String) fields.get(5).get("value");
        assertThat(content).hasSize(DiscordReportMessageFactory.CONTENT_PREVIEW_MAX_LENGTH + 1).endsWith("…");
    }

    @Test
    @DisplayName("작성자가 없는 대상(시스템 메시지)도 메시지를 만든다")
    void create_작성자가_없어도_만든다() {
        // given
        Report report = threadMessageReport(ReportTargetSnapshot.of(null, "[SYSTEM]", null));

        // when
        List<Map<String, Object>> fields = fields(sut.create(report));

        // then
        assertThat(fields).extracting(field -> field.get("value")).contains("(작성자 없음)", "[SYSTEM]");
    }

    @Test
    @DisplayName("신고 내용으로 누군가 멘션되지 않도록 멘션을 막는다")
    void create_멘션을_막는다() {
        // given
        Report report = threadMessageReport(ReportTargetSnapshot.of(4L, "@everyone 확인", null));

        // when
        Map<String, Object> message = sut.create(report);

        // then
        assertThat(message.get("allowed_mentions")).isEqualTo(Map.of("parse", List.of()));
    }

    @Test
    @DisplayName("신고 승인과 문제 없음 버튼을 붙인다")
    @SuppressWarnings("unchecked")
    void create_조치_버튼을_붙인다() {
        // given
        Report report = threadMessageReport(ReportTargetSnapshot.of(4L, "내용", null));

        // when
        List<Map<String, Object>> rows = (List<Map<String, Object>>) sut.create(report).get("components");

        // then
        List<Map<String, Object>> buttons = (List<Map<String, Object>>) rows.get(0).get("components");
        assertThat(buttons).extracting(button -> button.get("custom_id"))
            .containsExactly("report:901:approve", "report:901:reject");
        assertThat(buttons).extracting(button -> button.get("disabled")).containsOnly(false);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> fields(Map<String, Object> message) {
        Map<String, Object> embed = ((List<Map<String, Object>>) message.get("embeds")).get(0);
        return (List<Map<String, Object>>) embed.get("fields");
    }

    private Report threadMessageReport(ReportTargetSnapshot snapshot) {
        Report report = Report.createThreadMessage(1L, 11L, 33L, ReportReason.ABUSE, snapshot);
        ReflectionTestUtils.setField(report, "id", 901L);
        ReflectionTestUtils.setField(report, "createdAt", Instant.parse("2026-10-26T09:00:00Z"));
        return report;
    }
}
