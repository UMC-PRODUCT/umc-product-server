package com.umc.product.report.adapter.out.delivery.discord;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 신고 운영 채널(Discord 봇) 설정입니다.
 *
 * @param botToken  봇 토큰. 비밀값이므로 환경변수({@code REPORT_DISCORD_BOT_TOKEN})로만 주입합니다.
 * @param channelId 신고 메시지를 보낼 채널 ID
 */
@ConfigurationProperties(prefix = "app.report.discord")
public record ReportDiscordProperties(
    String botToken,
    String channelId
) {

    /**
     * 봇 토큰과 채널 ID가 모두 있어야 메시지를 보낼 수 있습니다.
     */
    public boolean isDeliveryConfigured() {
        return hasText(botToken) && hasText(channelId);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
