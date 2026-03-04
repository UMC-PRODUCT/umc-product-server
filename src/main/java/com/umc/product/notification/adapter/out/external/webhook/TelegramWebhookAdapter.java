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
@ConditionalOnProperty(name = "app.webhook.telegram.bot-token")
public class TelegramWebhookAdapter implements SendWebhookPort {

    private static final String TELEGRAM_API_URL = "https://api.telegram.org/bot{botToken}/sendMessage";
    private static final int MAX_MESSAGE_LENGTH = 4096;

    private final RestClient restClient;
    private final String botToken;
    private final String chatId;

    public TelegramWebhookAdapter(
        RestClient restClient,
        @Value("${app.webhook.telegram.bot-token}") String botToken,
        @Value("${app.webhook.telegram.chat-id}") String chatId
    ) {
        this.restClient = restClient;
        this.botToken = botToken;
        this.chatId = chatId;
    }

    @Override
    public void send(String title, String content) {
        String escapedTitle = escapeMarkdown(title);
        String escapedContent = escapeMarkdown(content);
        int overhead = escapedTitle.length() + 10;
        List<String> chunks = splitContent(escapedContent, MAX_MESSAGE_LENGTH - overhead);
        int totalParts = chunks.size();

        for (int i = 0; i < totalParts; i++) {
            String partTitle = totalParts == 1
                ? "*" + escapedTitle + "*"
                : "*" + escapedTitle + " \\(" + (i + 1) + "/" + totalParts + "\\)*";
            String text = partTitle + "\n" + chunks.get(i);

            restClient.post()
                .uri(TELEGRAM_API_URL, botToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                    "chat_id", chatId,
                    "text", text,
                    "parse_mode", "MarkdownV2"
                ))
                .retrieve()
                .toBodilessEntity();
        }

        log.debug("Telegram 웹훅 전송 완료: title={}, parts={}", title, totalParts);
    }

    @Override
    public WebhookPlatform platform() {
        return WebhookPlatform.TELEGRAM;
    }

    private String escapeMarkdown(String text) {
        return text.replaceAll("([_\\[\\]()~`>#+\\-=|{}.!])", "\\\\$1");
    }

    private List<String> splitContent(String content, int maxLength) {
        if (maxLength <= 0) {
            maxLength = MAX_MESSAGE_LENGTH;
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
