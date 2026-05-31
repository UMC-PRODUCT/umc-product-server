package com.umc.product.figma.adapter.in.web.support;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("DiscordWebhookUrlMasker")
class DiscordWebhookUrlMaskerTest {

    @Test
    @DisplayName("정상_webhook_URL_은_id_와_token_의_마지막_4자만_남기고_가린다")
    void 정상_마스킹() {
        String url = "https://discord.com/api/webhooks/123456789012345678/abcdefghijklmnopqrstuvwxyz1234";

        String masked = DiscordWebhookUrlMasker.mask(url);

        assertThat(masked).isEqualTo("https://discord.com/api/webhooks/****5678/****1234");
    }

    @Test
    @DisplayName("null_또는_빈_문자열은_그대로_반환한다")
    void null_또는_blank_그대로() {
        assertThat(DiscordWebhookUrlMasker.mask(null)).isNull();
        assertThat(DiscordWebhookUrlMasker.mask("")).isEmpty();
        assertThat(DiscordWebhookUrlMasker.mask("   ")).isEqualTo("   ");
    }

    @Test
    @DisplayName("token_이_4자_이하면_별표만_노출하고_평문_조각을_남기지_않는다")
    void 짧은_token_은_전부_가림() {
        String url = "https://discord.com/api/webhooks/123/abc";

        String masked = DiscordWebhookUrlMasker.mask(url);

        assertThat(masked).isEqualTo("https://discord.com/api/webhooks/****/****");
    }

    @Test
    @DisplayName("URL_에_슬래시가_없는_비정상_입력은_전부_가린다")
    void 슬래시_없는_입력은_전부_가림() {
        assertThat(DiscordWebhookUrlMasker.mask("nothing-to-see-here")).isEqualTo("****");
    }
}
