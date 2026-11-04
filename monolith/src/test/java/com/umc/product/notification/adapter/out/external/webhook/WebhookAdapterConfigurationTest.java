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
    @DisplayName("Discord 웹훅 URL이 빈 문자열이면 어댑터를 등록하지 않는다")
    void discord_웹훅_url이_빈_문자열이면_어댑터를_등록하지_않는다() {
        // given / when
        contextRunner.withPropertyValues("app.webhook.discord.url=").run(context -> {
            // then
            assertThat(context).hasNotFailed();
            assertThat(context).doesNotHaveBean(SendWebhookPort.class);
        });
    }

    @Test
    @DisplayName("Discord 웹훅 설정 키가 없으면 어댑터를 등록하지 않는다")
    void discord_웹훅_설정_키가_없으면_어댑터를_등록하지_않는다() {
        // given / when
        contextRunner.run(context -> {
            // then
            assertThat(context).hasNotFailed();
            assertThat(context).doesNotHaveBean(SendWebhookPort.class);
        });
    }

    @Test
    @DisplayName("Discord 웹훅 URL이 있으면 Discord 어댑터만 등록한다")
    void discord_웹훅_url이_있으면_discord_어댑터만_등록한다() {
        // given / when
        contextRunner.withPropertyValues(
            "app.webhook.discord.url=https://discord.example.test/api/webhooks/1/abc"
        ).run(context -> {
            // then
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(SendWebhookPort.class);
            assertThat(context).hasSingleBean(DiscordWebhookAdapter.class);
        });
    }

    @Configuration(proxyBeanMethods = false)
    @Import(DiscordWebhookAdapter.class)
    static class WebhookConfiguration {

        @Bean
        RestClient restClient() {
            return RestClient.builder().build();
        }
    }
}
