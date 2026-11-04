package com.umc.product.notification.adapter.out.external.webhook;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestClient;

import com.umc.product.notification.application.port.out.SendWebhookPort;

class WebhookAdapterConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withUserConfiguration(WebhookConfiguration.class);

    @Test
    @DisplayName("웹훅 설정이 모두 빈 문자열이면 어댑터를 등록하지 않는다")
    void 웹훅_설정이_모두_빈_문자열이면_어댑터를_등록하지_않는다() {
        // given / when
        contextRunner.withPropertyValues(
            "app.webhook.discord.url=",
            "app.webhook.slack.url=",
            "app.webhook.telegram.bot-token=",
            "app.webhook.telegram.chat-id="
        ).run(context -> {
            // then
            assertThat(context).hasNotFailed();
            assertThat(context).doesNotHaveBean(SendWebhookPort.class);
        });
    }

    @Test
    @DisplayName("웹훅 설정 키가 없으면 어댑터를 등록하지 않는다")
    void 웹훅_설정_키가_없으면_어댑터를_등록하지_않는다() {
        // given / when
        contextRunner.run(context -> {
            // then
            assertThat(context).hasNotFailed();
            assertThat(context).doesNotHaveBean(SendWebhookPort.class);
        });
    }

    @Test
    @DisplayName("값이 있는 플랫폼의 어댑터만 등록한다")
    void 값이_있는_플랫폼의_어댑터만_등록한다() {
        // given / when
        contextRunner.withPropertyValues(
            "app.webhook.discord.url=https://discord.example.test/api/webhooks/1/abc",
            "app.webhook.slack.url=",
            "app.webhook.telegram.bot-token=",
            "app.webhook.telegram.chat-id="
        ).run(context -> {
            // then
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(SendWebhookPort.class);
            assertThat(context).hasSingleBean(DiscordWebhookAdapter.class);
            assertThat(context).doesNotHaveBean(SlackWebhookAdapter.class);
            assertThat(context).doesNotHaveBean(TelegramWebhookAdapter.class);
        });
    }

    @Test
    @DisplayName("Telegram은 bot-token과 chat-id가 모두 있어야 등록한다")
    void Telegram은_bot_token과_chat_id가_모두_있어야_등록한다() {
        // given / when
        contextRunner.withPropertyValues(
            "app.webhook.telegram.bot-token=123456:test-token",
            "app.webhook.telegram.chat-id="
        ).run(context -> {
            // then
            assertThat(context).hasNotFailed();
            assertThat(context).doesNotHaveBean(TelegramWebhookAdapter.class);
        });

        contextRunner.withPropertyValues(
            "app.webhook.telegram.bot-token=123456:test-token",
            "app.webhook.telegram.chat-id=-1001"
        ).run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(TelegramWebhookAdapter.class);
        });
    }

    @Configuration(proxyBeanMethods = false)
    @Import({DiscordWebhookAdapter.class, SlackWebhookAdapter.class, TelegramWebhookAdapter.class})
    static class WebhookConfiguration {

        @Bean
        RestClient restClient() {
            return RestClient.builder().build();
        }
    }
}
