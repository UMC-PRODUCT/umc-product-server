package com.umc.product.report.adapter.out.delivery.discord;

import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.umc.product.global.logging.ExternalApiCallLogger;
import com.umc.product.report.application.port.out.ReportDeliveryPort;
import com.umc.product.report.domain.Report;

/**
 * Discord 봇으로 신고 채널에 메시지를 보냅니다.
 * <p>
 * 봇 토큰이나 채널 ID가 없으면 비활성으로 응답하므로, 신고는 보내지 않고 전달 대기(PENDING)로 남습니다.
 */
@Component
public class DiscordBotReportDeliveryAdapter implements ReportDeliveryPort {

    static final String CREATE_MESSAGE_URL = "https://discord.com/api/v10/channels/{channelId}/messages";
    // Discord는 봇 요청의 User-Agent를 "DiscordBot (URL, 버전)" 형식으로 요구하며, 형식이 다르면 요청을 막을 수 있습니다.
    static final String USER_AGENT = "DiscordBot (https://github.com/UMC-PRODUCT/umc-product-server, 1.0)";

    private final RestClient restClient;
    private final ReportDiscordProperties properties;
    private final DiscordReportMessageFactory messageFactory;

    public DiscordBotReportDeliveryAdapter(
        RestClient restClient,
        ReportDiscordProperties properties,
        DiscordReportMessageFactory messageFactory
    ) {
        this.restClient = restClient;
        this.properties = properties;
        this.messageFactory = messageFactory;
    }

    @Override
    public boolean isEnabled() {
        return properties.isDeliveryConfigured();
    }

    @Override
    public void deliver(Report report) {
        if (!isEnabled()) {
            throw new IllegalStateException("신고 운영 채널(Discord 봇)이 설정되지 않았습니다.");
        }
        Map<String, Object> message = messageFactory.create(report);
        ExternalApiCallLogger.measure("DISCORD", "SEND_REPORT", () ->
            restClient.post()
                .uri(CREATE_MESSAGE_URL, properties.channelId())
                .header(HttpHeaders.AUTHORIZATION, "Bot " + properties.botToken())
                .header(HttpHeaders.USER_AGENT, USER_AGENT)
                .contentType(MediaType.APPLICATION_JSON)
                .body(message)
                .retrieve()
                .toBodilessEntity()
        );
    }
}
