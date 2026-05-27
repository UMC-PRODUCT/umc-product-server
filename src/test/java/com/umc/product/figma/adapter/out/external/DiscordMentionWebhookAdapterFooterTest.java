package com.umc.product.figma.adapter.out.external;

import static org.assertj.core.api.Assertions.assertThat;

import com.umc.product.figma.application.port.out.dto.DiscordDomainBatchMessage;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

/**
 * Discord embed footer 의 환경 라벨 ([ENV: ...]) 강제 표시 검증. Figma_댓글_dev_prod_중복_방지_계획 §L4.
 */
@DisplayName("DiscordMentionWebhookAdapter — footer 환경 라벨")
class DiscordMentionWebhookAdapterFooterTest {

    @Test
    @DisplayName("환경=dev 면 footer 가 [ENV: dev] 로 시작한다")
    void footer_환경_라벨_dev() {
        DiscordMentionWebhookAdapter adapter =
            new DiscordMentionWebhookAdapter(RestClient.create(), "dev");
        DiscordDomainBatchMessage message = new DiscordDomainBatchMessage(
            "https://discord.example/wh", "auth", List.of(),
            Instant.parse("2026-05-08T03:00:00Z"),
            Instant.parse("2026-05-08T03:05:00Z"),
            List.of()
        );

        String footer = ReflectionTestUtils.invokeMethod(adapter, "buildFooterText", message);

        assertThat(footer).startsWith("[ENV: dev] ");
        assertThat(footer).contains("Figma · ");
        assertThat(footer).endsWith("KST");
    }

    @Test
    @DisplayName("환경=prod 면 footer 가 [ENV: prod] 로 시작한다")
    void footer_환경_라벨_prod() {
        DiscordMentionWebhookAdapter adapter =
            new DiscordMentionWebhookAdapter(RestClient.create(), "prod");
        DiscordDomainBatchMessage message = new DiscordDomainBatchMessage(
            "https://discord.example/wh", "challenger", List.of(),
            Instant.parse("2026-05-08T03:00:00Z"),
            Instant.parse("2026-05-08T03:05:00Z"),
            List.of()
        );

        String footer = ReflectionTestUtils.invokeMethod(adapter, "buildFooterText", message);

        assertThat(footer).startsWith("[ENV: prod] ");
    }

    @Test
    @DisplayName("windowFrom / windowTo 가 모두 null 이어도 환경 라벨은 항상 prefix 된다")
    void footer_환경_라벨_시간창_없는_경우() {
        DiscordMentionWebhookAdapter adapter =
            new DiscordMentionWebhookAdapter(RestClient.create(), "staging");
        DiscordDomainBatchMessage message = new DiscordDomainBatchMessage(
            "https://discord.example/wh", "figma", List.of(),
            null, null, List.of()
        );

        String footer = ReflectionTestUtils.invokeMethod(adapter, "buildFooterText", message);

        assertThat(footer).isEqualTo("[ENV: staging] Figma comment forwarder");
    }
}
