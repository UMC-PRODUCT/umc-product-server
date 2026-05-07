package com.umc.product.figma.adapter.out.external;

import com.umc.product.figma.application.port.out.SendDiscordMentionPort;
import com.umc.product.figma.application.port.out.dto.DiscordMentionMessage;
import com.umc.product.figma.domain.exception.FigmaDomainException;
import com.umc.product.figma.domain.exception.FigmaErrorCode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * Figma 댓글을 Discord 로 포워딩하는 webhook 어댑터.
 *
 * 메시지 구조:
 * <ul>
 *   <li>{@code content}: 멘션 문자열 모음만 — 알림이 발생하는 영역</li>
 *   <li>{@code embeds}: title / description / url / color / fields / footer / timestamp 로
 *       포맷된 1건의 embed — 가독성 영역</li>
 * </ul>
 * embed 본문에 들어간 mention 문자열은 알림이 발생하지 않으므로,
 * 멘션은 반드시 외부 {@code content} 에 두고 embed 는 시각 표현 전담.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DiscordMentionWebhookAdapter implements SendDiscordMentionPort {

    /** Figma brand color (hex 0xF24E1E → decimal). embed 좌측 strip 색상으로 노출된다. */
    private static final int EMBED_COLOR_FIGMA = 0xF24E1E;
    private static final int EMBED_TITLE_MAX = 256;
    private static final int EMBED_DESCRIPTION_MAX = 4096;
    private static final int EMBED_FIELD_VALUE_MAX = 1024;

    private final RestClient restClient;

    @Override
    public void send(DiscordMentionMessage message) {
        String mentionLine = message.mentionRenders() == null || message.mentionRenders().isEmpty()
            ? ""
            : String.join(" ", message.mentionRenders());

        Map<String, Object> embed = buildEmbed(message);

        Map<String, Object> payload = Map.of(
            "content", mentionLine,
            "embeds", List.of(embed),
            "allowed_mentions", Map.of("parse", List.of("roles", "users"))
        );

        try {
            restClient.post()
                .uri(message.webhookUrl())
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .toBodilessEntity();
            log.debug("Discord embed 멘션 전송 완료: domainKey={}, mentions={}",
                message.domainKey(), message.mentionRenders() == null ? 0 : message.mentionRenders().size());
        } catch (RestClientResponseException e) {
            log.error("Discord embed 멘션 전송 실패: status={}, body={}",
                e.getStatusCode(), e.getResponseBodyAsString());
            throw new FigmaDomainException(FigmaErrorCode.DISCORD_MENTION_SEND_FAILED);
        }
    }

    private Map<String, Object> buildEmbed(DiscordMentionMessage message) {
        Map<String, Object> embed = new LinkedHashMap<>();
        embed.put("title", truncate("[Figma] " + safe(message.fileDisplayName()) + " 새 댓글", EMBED_TITLE_MAX));
        embed.put("description", truncate(safe(message.message()), EMBED_DESCRIPTION_MAX));
        embed.put("url", safe(message.commentLink()));
        embed.put("color", EMBED_COLOR_FIGMA);

        Map<String, Object> author = new LinkedHashMap<>();
        author.put("name", safe(message.authorName()));
        embed.put("author", author);

        List<Map<String, Object>> fields = new ArrayList<>();
        fields.add(field("Domain", message.domainKey() == null ? "(unmatched)" : message.domainKey(), true));
        fields.add(field("Page", message.pageName() == null ? "-" : message.pageName(), true));
        embed.put("fields", fields);

        if (message.createdAt() != null) {
            embed.put("timestamp", message.createdAt().toString());
        }

        Map<String, Object> footer = new LinkedHashMap<>();
        footer.put("text", "Figma comment forwarder");
        embed.put("footer", footer);

        return embed;
    }

    private Map<String, Object> field(String name, String value, boolean inline) {
        Map<String, Object> field = new LinkedHashMap<>();
        field.put("name", name);
        field.put("value", truncate(safe(value), EMBED_FIELD_VALUE_MAX));
        field.put("inline", inline);
        return field;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String truncate(String value, int max) {
        if (value == null) {
            return "";
        }
        if (value.length() <= max) {
            return value;
        }
        return value.substring(0, max - 1) + "…";
    }
}
