package com.umc.product.report.adapter.out.external;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.never;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.umc.product.report.domain.Report;
import com.umc.product.report.domain.ReportTargetSnapshot;
import com.umc.product.report.domain.enums.ReportReason;

@DisplayName("Discord 신고 전달 어댑터")
class DiscordReportDeliveryAdapterTest {

    private static final String WEBHOOK_URL = "https://discord.test/api/webhooks/report";

    private final RestClient.Builder builder = RestClient.builder();
    private final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

    @Test
    @DisplayName("신고 정보와 스냅샷을 담아 웹훅으로 보낸다")
    void deliver_웹훅으로_보낸다() {
        // given
        DiscordReportDeliveryAdapter sut = new DiscordReportDeliveryAdapter(builder.build(), WEBHOOK_URL);
        server.expect(once(), requestTo(WEBHOOK_URL))
            .andExpect(method(HttpMethod.POST))
            .andExpect(jsonPath("$.embeds[0].title").value("신고 접수 #901"))
            .andRespond(withSuccess());

        // when
        sut.deliver(threadMessageReport("신고 대상 메시지"));

        // then
        server.verify();
    }

    @Test
    @DisplayName("웹훅 응답이 실패하면 예외를 던진다")
    void deliver_실패하면_예외를_던진다() {
        // given
        DiscordReportDeliveryAdapter sut = new DiscordReportDeliveryAdapter(builder.build(), WEBHOOK_URL);
        server.expect(once(), requestTo(WEBHOOK_URL)).andRespond(withServerError());

        // when & then
        assertThatThrownBy(() -> sut.deliver(threadMessageReport("신고 대상 메시지")))
            .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("웹훅 URL이 없으면 비활성으로 보고, 보내려 하면 예외를 던진다")
    void URL이_없으면_비활성이다() {
        // given
        DiscordReportDeliveryAdapter disabled = new DiscordReportDeliveryAdapter(builder.build(), "");
        server.expect(never(), requestTo(WEBHOOK_URL));

        // when & then
        assertThat(disabled.isEnabled()).isFalse();
        assertThat(new DiscordReportDeliveryAdapter(builder.build(), WEBHOOK_URL).isEnabled()).isTrue();
        assertThatThrownBy(() -> disabled.deliver(threadMessageReport("신고 대상 메시지")))
            .isInstanceOf(IllegalStateException.class);
        server.verify();
    }

    @Test
    @DisplayName("메시지에는 사유·대상·위치·작성자와 200자로 자른 내용, 조치 안내를 담는다")
    @SuppressWarnings("unchecked")
    void payload_검토에_필요한_정보만_담는다() {
        // given
        DiscordReportDeliveryAdapter sut = new DiscordReportDeliveryAdapter(builder.build(), WEBHOOK_URL);
        String longContent = "가".repeat(DiscordReportDeliveryAdapter.CONTENT_PREVIEW_MAX_LENGTH + 50);

        // when
        Map<String, Object> payload = sut.payload(threadMessageReport(longContent));

        // then
        Map<String, Object> embed = ((List<Map<String, Object>>) payload.get("embeds")).get(0);
        List<Map<String, Object>> fields = (List<Map<String, Object>>) embed.get("fields");
        assertThat(fields).extracting(field -> field.get("name"))
            .containsExactly("신고 ID", "사유", "대상", "위치", "작성자 member ID", "내용", "접수 시각");
        assertThat(fields).extracting(field -> field.get("value"))
            .contains("욕설/비방", "스레드 메시지 #33 (스레드 #11)", "스레드 제목", "4", "2026-10-26 18:00");
        String content = (String) fields.get(5).get("value");
        assertThat(content).hasSize(DiscordReportDeliveryAdapter.CONTENT_PREVIEW_MAX_LENGTH + 1).endsWith("…");
        assertThat(((Map<String, Object>) embed.get("footer")).get("text")).asString().contains("답글");
    }

    @Test
    @DisplayName("작성자가 없는 시스템 메시지는 작성자를 '없음'으로 표시한다")
    @SuppressWarnings("unchecked")
    void payload_작성자가_없으면_없음으로_표시한다() {
        // given
        DiscordReportDeliveryAdapter sut = new DiscordReportDeliveryAdapter(builder.build(), WEBHOOK_URL);
        Report report = Report.createThreadMessage(
            1L, 11L, 33L, ReportReason.SPAM, ReportTargetSnapshot.of(null, "입장했습니다.", "스레드 제목")
        );
        ReflectionTestUtils.setField(report, "id", 902L);

        // when
        Map<String, Object> payload = sut.payload(report);

        // then
        Map<String, Object> embed = ((List<Map<String, Object>>) payload.get("embeds")).get(0);
        List<Map<String, Object>> fields = (List<Map<String, Object>>) embed.get("fields");
        assertThat(fields).extracting(field -> field.get("value")).contains("없음 (시스템 메시지)");
    }

    @Test
    @DisplayName("내용을 자를 때 이모지가 중간에서 잘리지 않는다")
    @SuppressWarnings("unchecked")
    void payload_이모지를_중간에서_자르지_않는다() {
        // given
        DiscordReportDeliveryAdapter sut = new DiscordReportDeliveryAdapter(builder.build(), WEBHOOK_URL);
        String emojis = "😀".repeat(DiscordReportDeliveryAdapter.CONTENT_PREVIEW_MAX_LENGTH + 10);

        // when
        Map<String, Object> payload = sut.payload(threadMessageReport(emojis));

        // then
        Map<String, Object> embed = ((List<Map<String, Object>>) payload.get("embeds")).get(0);
        List<Map<String, Object>> fields = (List<Map<String, Object>>) embed.get("fields");
        String content = (String) fields.get(5).get("value");
        assertThat(content).isEqualTo("😀".repeat(DiscordReportDeliveryAdapter.CONTENT_PREVIEW_MAX_LENGTH) + "…");
    }

    private Report threadMessageReport(String content) {
        Report report = Report.createThreadMessage(
            1L, 11L, 33L, ReportReason.ABUSE, ReportTargetSnapshot.of(4L, content, "스레드 제목")
        );
        ReflectionTestUtils.setField(report, "id", 901L);
        ReflectionTestUtils.setField(report, "createdAt", Instant.parse("2026-10-26T09:00:00Z"));
        return report;
    }
}
