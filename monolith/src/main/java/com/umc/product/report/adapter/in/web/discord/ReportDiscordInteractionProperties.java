package com.umc.product.report.adapter.in.web.discord;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Discord 상호작용 콜백을 받을 때 쓰는 설정입니다.
 * <p>
 * 메시지 전송 설정({@code ReportDiscordProperties})과 같은 접두사를 쓰지만, 받는 쪽과 보내는 쪽 어댑터가 서로를 참조하지 않도록 따로 둡니다.
 *
 * @param publicKey       Discord 앱 공개키(hex). 환경변수({@code REPORT_DISCORD_PUBLIC_KEY})로 주입합니다.
 * @param guildId         신고를 처리할 수 있는 Discord 서버 ID
 * @param moderatorRoleId 신고를 처리할 수 있는 역할 ID. 쉼표로 여러 개를 넣을 수 있습니다(예: {@code 111,222})
 */
@ConfigurationProperties(prefix = "app.report.discord")
public record ReportDiscordInteractionProperties(
    String publicKey,
    String guildId,
    String moderatorRoleId
) {

    public boolean hasPublicKey() {
        return hasText(publicKey);
    }

    /**
     * 쉼표로 구분된 신고처리 역할 ID를 집합으로 돌려줍니다. 앞뒤 공백은 지우고 빈 항목은 버립니다.
     */
    public Set<String> moderatorRoleIds() {
        if (!hasText(moderatorRoleId)) {
            return Set.of();
        }
        return Arrays.stream(moderatorRoleId.split(","))
            .map(String::trim)
            .filter(roleId -> !roleId.isEmpty())
            .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * 서버 ID와 신고처리 역할 ID가 하나 이상 있어야 신고를 처리할 수 있습니다.
     */
    public boolean hasModeratorScope() {
        return hasText(guildId) && !moderatorRoleIds().isEmpty();
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
