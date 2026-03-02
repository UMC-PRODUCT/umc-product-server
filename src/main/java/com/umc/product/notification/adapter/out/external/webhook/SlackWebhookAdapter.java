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
@ConditionalOnProperty(name = "app.webhook.slack.url")
public class SlackWebhookAdapter implements SendWebhookPort {

    private static final int MAX_TEXT_LENGTH = 3000;

    private final RestClient restClient;
    private final String webhookUrl;

    public SlackWebhookAdapter(
        RestClient restClient,
        @Value("${app.webhook.slack.url}") String webhookUrl
    ) {
        this.restClient = restClient;
        this.webhookUrl = webhookUrl;
    }

    @Override
    public void send(String title, String content) {
        List<String> chunks = splitContent(content, MAX_TEXT_LENGTH - title.length() - 10);
        int totalParts = chunks.size();

        for (int i = 0; i < totalParts; i++) {
            String partTitle = totalParts == 1
                ? "*" + title + "*"
                : "*" + title + " (" + (i + 1) + "/" + totalParts + ")*";
            String text = partTitle + "\n" + chunks.get(i);

            restClient.post()
                .uri(webhookUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("text", text))
                .retrieve()
                .toBodilessEntity();
        }

        log.debug("Slack 웹훅 전송 완료: title={}, parts={}", title, totalParts);
    }

    @Override
    public WebhookPlatform platform() {
        return WebhookPlatform.SLACK;
    }

    private List<String> splitContent(String content, int maxLength) {
        if (maxLength <= 0) {
            maxLength = MAX_TEXT_LENGTH;
        }
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
