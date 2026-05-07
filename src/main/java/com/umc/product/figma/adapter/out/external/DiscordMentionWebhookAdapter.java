package com.umc.product.figma.adapter.out.external;

import com.umc.product.figma.application.port.out.SendDiscordMentionPort;
import com.umc.product.figma.application.port.out.dto.DiscordDomainBatchMessage;
import com.umc.product.figma.application.port.out.dto.DiscordDomainBatchMessage.CommentEntry;
import com.umc.product.figma.domain.exception.FigmaDomainException;
import com.umc.product.figma.domain.exception.FigmaErrorCode;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
 * 도메인 단위로 묶인 댓글 batch 를 Discord 로 발송하는 webhook 어댑터.
 * <p>
 * Discord embed 제약:
 * <ul>
 *   <li>embed 1건당 fields 최대 25</li>
 *   <li>메시지 1건당 embeds 최대 10</li>
 *   <li>embed 1건의 합산 문자수 ≤ 6000</li>
 *   <li>field name ≤ 256, field value ≤ 1024</li>
 *   <li>embed description ≤ 4096</li>
 * </ul>
 * <p>
 * 본 어댑터는 댓글 1건 = field 1개 로 매핑하고, 25개씩 chunk 해 embed 를 만든다.
 * embed 가 10개를 넘으면 메시지를 분할 발송한다 (Discord rate limit 까지 자동 분할).
 * 멘션은 첫 메시지의 외부 content 에만 포함되어 알림이 한 번만 울리도록 한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DiscordMentionWebhookAdapter implements SendDiscordMentionPort {

    private static final int EMBED_COLOR_FIGMA = 0xF24E1E;
    private static final int FIELDS_PER_EMBED = 25;
    private static final int EMBEDS_PER_MESSAGE = 10;
    private static final int FIELD_NAME_MAX = 256;
    private static final int FIELD_VALUE_MAX = 1024;
    /**
     * 댓글 사이 시각적 여백을 위해 각 field value 끝에 붙이는 zero-width space 라인. Discord 가 non-inline field 들을 거의 붙여서 렌더링해 가독성이 떨어지므로,
     * 마지막 줄 다음에 빈 줄 한 칸을 강제로 추가한다 (field 수를 늘리지 않으므로 25 댓글/embed 수용은 유지).
     */
    private static final String FIELD_VALUE_TRAILING_GAP = "\n\u200B";
    private static final ZoneId FOOTER_ZONE = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter FOOTER_FORMAT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(FOOTER_ZONE);
    private static final String FOOTER_ZONE_LABEL = "KST";

    private final RestClient restClient;

    @Override
    public void send(DiscordDomainBatchMessage message) {
        if (message.comments() == null || message.comments().isEmpty()) {
            return;
        }

        String mentionLine = renderMentionLine(message.mentionRenders());
        List<List<Map<String, Object>>> embedPages = buildEmbedPages(message);

        for (int i = 0; i < embedPages.size(); i++) {
            boolean firstMessage = (i == 0);
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("content", firstMessage ? mentionLine : "");
            payload.put("embeds", embedPages.get(i));
            payload.put("allowed_mentions", Map.of("parse", List.of("roles", "users")));

            try {
                restClient.post()
                    .uri(message.webhookUrl())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
                log.debug("Discord domain batch 전송 완료: domainKey={}, page={}/{}, comments={}",
                    message.domainKey(), i + 1, embedPages.size(), message.comments().size());
            } catch (RestClientResponseException e) {
                log.error("Discord domain batch 전송 실패: domainKey={}, page={}/{}, status={}, body={}",
                    message.domainKey(), i + 1, embedPages.size(),
                    e.getStatusCode(), e.getResponseBodyAsString());
                throw new FigmaDomainException(FigmaErrorCode.DISCORD_MENTION_SEND_FAILED);
            }
        }
    }

    private String renderMentionLine(List<String> mentionRenders) {
        if (mentionRenders == null || mentionRenders.isEmpty()) {
            return "";
        }
        return String.join(" ", mentionRenders);
    }

    /**
     * 댓글 리스트를 25개씩 묶어 embed 들을 만들고, 다시 10개씩 묶어 메시지(payload) 단위로 분할한다.
     */
    private List<List<Map<String, Object>>> buildEmbedPages(DiscordDomainBatchMessage message) {
        List<CommentEntry> comments = message.comments();
        int totalComments = comments.size();
        int totalEmbeds = (totalComments + FIELDS_PER_EMBED - 1) / FIELDS_PER_EMBED;

        List<Map<String, Object>> allEmbeds = new ArrayList<>(totalEmbeds);
        for (int e = 0; e < totalEmbeds; e++) {
            int from = e * FIELDS_PER_EMBED;
            int to = Math.min(from + FIELDS_PER_EMBED, totalComments);
            allEmbeds.add(buildEmbed(message, comments.subList(from, to), e + 1, totalEmbeds));
        }

        List<List<Map<String, Object>>> pages = new ArrayList<>();
        for (int p = 0; p < allEmbeds.size(); p += EMBEDS_PER_MESSAGE) {
            pages.add(new ArrayList<>(allEmbeds.subList(p, Math.min(p + EMBEDS_PER_MESSAGE, allEmbeds.size()))));
        }
        return pages;
    }

    private Map<String, Object> buildEmbed(
        DiscordDomainBatchMessage message,
        List<CommentEntry> chunk,
        int pageIndex,
        int totalPages
    ) {
        Map<String, Object> embed = new LinkedHashMap<>();
        String titleSuffix = totalPages == 1 ? "" : " (" + pageIndex + "/" + totalPages + ")";
        embed.put("title", message.domainKey() + " 관련 새로운 논의사항이 "
            + message.comments().size() + "건 있어요! " + titleSuffix);
        embed.put("color", EMBED_COLOR_FIGMA);

        List<Map<String, Object>> fields = new ArrayList<>(chunk.size());
        for (CommentEntry c : chunk) {
            fields.add(buildField(c));
        }
        embed.put("fields", fields);

        Map<String, Object> footer = new LinkedHashMap<>();
        footer.put("text", buildFooter(message));
        embed.put("footer", footer);

        // 가장 최근 댓글 시각을 embed timestamp 로 노출
        chunk.stream()
            .map(CommentEntry::createdAt)
            .filter(java.util.Objects::nonNull)
            .max(java.time.Instant::compareTo)
            .ifPresent(t -> embed.put("timestamp", t.toString()));

        return embed;
    }

    private Map<String, Object> buildField(CommentEntry c) {
        String pageSuffix = (c.pageName() == null || c.pageName().isBlank()) ? "" : " / " + c.pageName();
        String name = truncate(
            "\uD83C\uDF38 " + safe(c.authorName()) + " · " + safe(c.fileDisplayName()) + pageSuffix,
            FIELD_NAME_MAX
        );
        // 본문 + 링크 길이를 1024 - GAP 길이까지 자른 뒤 끝에 GAP 을 붙여 댓글 사이 빈 줄을 강제한다.
        String body = truncate(
            safe(c.message()) + "\n🔗 [바로가기](" + safe(c.commentLink()) + ")",
            FIELD_VALUE_MAX - FIELD_VALUE_TRAILING_GAP.length()
        );
        String value = body + FIELD_VALUE_TRAILING_GAP;

        Map<String, Object> field = new LinkedHashMap<>();
        field.put("name", name);
        field.put("value", value);
        field.put("inline", false);
        return field;
    }

    private String buildFooter(DiscordDomainBatchMessage message) {
        if (message.windowFrom() == null && message.windowTo() == null) {
            return "Figma comment forwarder";
        }
        String from = message.windowFrom() == null ? "-" : FOOTER_FORMAT.format(message.windowFrom());
        String to = message.windowTo() == null ? "-" : FOOTER_FORMAT.format(message.windowTo());
        return "Figma · " + from + " ~ " + to + " " + FOOTER_ZONE_LABEL;
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
