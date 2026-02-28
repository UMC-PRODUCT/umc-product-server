package com.umc.product.notification.adapter.out.external.webhook;

import com.umc.product.notification.application.port.out.SendWebhookPort;
import com.umc.product.notification.domain.WebhookPlatform;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.webhook.discord.url")
public class DiscordWebhookAdapter implements SendWebhookPort {

    private static final int MAX_EMBED_DESCRIPTION_LENGTH = 4096;

    private final RestClient restClient;
    private final String webhookUrl;

    public DiscordWebhookAdapter(
        RestClient restClient,
        @Value("${app.webhook.discord.url}") String webhookUrl
    ) {
        this.restClient = restClient;
        this.webhookUrl = webhookUrl;
    }

    @Override
    public void send(String title, String content) {
        List<String> chunks = splitContent(content, MAX_EMBED_DESCRIPTION_LENGTH);
        int totalParts = chunks.size();

        for (int i = 0; i < totalParts; i++) {
            String partTitle = totalParts == 1
                ? title
                : title + " (" + (i + 1) + "/" + totalParts + ")";

            Map<String, Object> embed = Map.of(
                "title", partTitle,
                "description", chunks.get(i)
            );

            restClient.post()
                .uri(webhookUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("embeds", List.of(embed)))
                .retrieve()
                .toBodilessEntity();
        }

        log.debug("Discord 웹훅 전송 완료: title={}, parts={}", title, totalParts);
    }

    @Override
    public WebhookPlatform platform() {
        return WebhookPlatform.DISCORD;
    }

    private List<String> splitContent(String content, int maxLength) {
        if (content.length() <= maxLength) {
            return List.of(content);
        }

        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < content.length()) {
            int end = Math.min(start + maxLength, content.length());
            if (end < content.length()) {
                int lastNewline = content.lastIndexOf('\n', end);
                if (lastNewline > start) {
                    end = lastNewline + 1;
                }
            }
            chunks.add(content.substring(start, end));
            start = end;
        }
        return chunks;
    }
}
