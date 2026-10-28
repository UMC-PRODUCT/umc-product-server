package com.umc.product.report.adapter.out.delivery.discord;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.umc.product.report.domain.Report;
import com.umc.product.report.domain.ReportTargetSnapshot;
import com.umc.product.report.domain.enums.ReportReason;

@DisplayName("Discord 봇 신고 전달 어댑터")
class DiscordBotReportDeliveryAdapterTest {

    private static final String BOT_TOKEN = "test-bot-token";
    private static final String CHANNEL_ID = "1000";
    private static final String MESSAGE_URL = "https://discord.com/api/v10/channels/1000/messages";

    private final RestClient.Builder builder = RestClient.builder();
    private final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

    @Test
    @DisplayName("봇 토큰으로 인증해 신고 채널에 메시지를 보낸다")
    void deliver_채널에_메시지를_보낸다() {
        // given
        DiscordBotReportDeliveryAdapter sut = adapter(BOT_TOKEN, CHANNEL_ID);
        server.expect(once(), requestTo(MESSAGE_URL))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Bot " + BOT_TOKEN))
            .andExpect(jsonPath("$.embeds[0].title").value("신고 접수 #901"))
            .andExpect(jsonPath("$.allowed_mentions.parse").isEmpty())
            .andRespond(withSuccess());

        // when
        sut.deliver(report());

        // then
        server.verify();
    }

    @Test
    @DisplayName("Discord 응답이 실패하면 예외를 던진다")
    void deliver_실패하면_예외를_던진다() {
        // given
        DiscordBotReportDeliveryAdapter sut = adapter(BOT_TOKEN, CHANNEL_ID);
        server.expect(once(), requestTo(MESSAGE_URL)).andRespond(withServerError());

        // when & then
        assertThatThrownBy(() -> sut.deliver(report())).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("봇 토큰이나 채널 ID가 없으면 비활성이고, 보내려 하면 예외를 던진다")
    void 설정이_없으면_비활성이다() {
        // given
        DiscordBotReportDeliveryAdapter noToken = adapter("", CHANNEL_ID);
        DiscordBotReportDeliveryAdapter noChannel = adapter(BOT_TOKEN, null);

        // when & then
        assertThat(noToken.isEnabled()).isFalse();
        assertThat(noChannel.isEnabled()).isFalse();
        assertThat(adapter(BOT_TOKEN, CHANNEL_ID).isEnabled()).isTrue();
        assertThatThrownBy(() -> noToken.deliver(report())).isInstanceOf(IllegalStateException.class);
    }

    private DiscordBotReportDeliveryAdapter adapter(String botToken, String channelId) {
        return new DiscordBotReportDeliveryAdapter(
            builder.build(),
            new ReportDiscordProperties(botToken, channelId),
            new DiscordReportMessageFactory()
        );
    }

    private Report report() {
        Report report = Report.createThreadMessage(
            1L, 11L, 33L, ReportReason.ABUSE, ReportTargetSnapshot.of(4L, "신고 대상 메시지", "스레드 제목")
        );
        ReflectionTestUtils.setField(report, "id", 901L);
        return report;
    }
}
