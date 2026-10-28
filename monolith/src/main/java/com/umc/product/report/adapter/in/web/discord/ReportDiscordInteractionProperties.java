package com.umc.product.report.adapter.in.web.discord;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Discord 상호작용 콜백을 받을 때 쓰는 설정입니다.
 * <p>
 * 메시지 전송 설정({@code ReportDiscordProperties})과 같은 접두사를 쓰지만, 받는 쪽과 보내는 쪽 어댑터가 서로를 참조하지 않도록 따로 둡니다.
 *
 * @param publicKey Discord 앱 공개키(hex). 환경변수({@code REPORT_DISCORD_PUBLIC_KEY})로 주입합니다.
 */
@ConfigurationProperties(prefix = "app.report.discord")
public record ReportDiscordInteractionProperties(
    String publicKey
) {

    public boolean hasPublicKey() {
        return publicKey != null && !publicKey.isBlank();
    }
}
