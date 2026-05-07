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
 * 본 어댑터는 댓글 1건 = field 1개 로 매핑하고, fields 25개 한도와 합산 5500자 budget 두 제약 하에
 * dynamic packing 으로 embed 를 만든다 (긴 댓글이 섞여도 6000자 합산 한도를 안전하게 회피).
 * embed 가 10개를 넘으면 메시지를 분할 발송한다 (Discord rate limit 까지 자동 분할).
 * 멘션은 첫 메시지의 외부 content 에만 포함되어 알림이 한 번만 울리도록 한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DiscordMentionWebhookAdapter implements SendDiscordMentionPort {

    private static final int EMBED_COLOR_FIGMA = 0x5DADE2;
    private static final int FIELDS_PER_EMBED = 25;
    private static final int EMBEDS_PER_MESSAGE = 10;
    private static final int FIELD_NAME_MAX = 256;
    private static final int FIELD_VALUE_MAX = 1024;
    /**
     * Discord embed 의 모든 텍스트 필드(title + description + fields의 name+value + footer.text + author.name) 합산 character 한도.
     * 한도 초과 시 400 BAD_REQUEST 와 함께 "Embed size exceeds maximum size of 6000".
     */
    private static final int EMBED_TOTAL_CHAR_MAX = 6000;
    
    /**
     * fields 외 나머지 (title / footer / author / 마진) 가 사용할 수 있는 reserve. title 은 보통 ~80자, footer 는 ~50자라 130자면 충분하지만,
     * surrogate pair 같은 다중 code unit 문자와 안전 마진을 감안해 500자로 잡는다. 즉 fields 의 합은 5500자까지.
     */
    private static final int EMBED_OVERHEAD_RESERVE = 500;
    private static final int EMBED_FIELDS_BUDGET = EMBED_TOTAL_CHAR_MAX - EMBED_OVERHEAD_RESERVE;
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
     * 댓글들을 fields 25개 한도 + character 5500자 budget 두 가지 제약으로 dynamic packing 한 뒤, 메시지당 embed 10개 단위로 페이지네이션한다. 6000자 합산
     * 한도로 인한 Discord 400 BAD_REQUEST 를 사전에 차단한다.
     */
    private List<List<Map<String, Object>>> buildEmbedPages(DiscordDomainBatchMessage message) {
        List<CommentEntry> comments = message.comments();

        // 1. 각 댓글을 field 로 미리 빌드해 실제 length 를 측정한다.
        List<Map<String, Object>> allFields = new ArrayList<>(comments.size());
        for (CommentEntry c : comments) {
            allFields.add(buildField(c));
        }

        // 2. fields 25개 + character 5500자 budget 으로 chunk 경계 결정.
        List<int[]> chunkRanges = packFieldsByBudget(allFields);

        // 3. 각 chunk 를 embed 로 빌드.
        List<Map<String, Object>> allEmbeds = new ArrayList<>(chunkRanges.size());
        int totalEmbeds = chunkRanges.size();
        for (int e = 0; e < totalEmbeds; e++) {
            int from = chunkRanges.get(e)[0];
            int to = chunkRanges.get(e)[1];
            allEmbeds.add(buildEmbed(
                message,
                comments.subList(from, to),
                allFields.subList(from, to),
                e + 1,
                totalEmbeds
            ));
        }

        // 4. 메시지당 embed 10개 페이지네이션.
        List<List<Map<String, Object>>> pages = new ArrayList<>();
        for (int p = 0; p < allEmbeds.size(); p += EMBEDS_PER_MESSAGE) {
            pages.add(new ArrayList<>(allEmbeds.subList(p, Math.min(p + EMBEDS_PER_MESSAGE, allEmbeds.size()))));
        }
        return pages;
    }

    /**
     * fields 를 (i) 25개 한도와 (ii) name+value 합산 5500자 budget 두 제약 하에 greedy 로 chunk 분할한다.
     *
     * @return 각 chunk 의 [fromInclusive, toExclusive] 범위 리스트
     */
    private List<int[]> packFieldsByBudget(List<Map<String, Object>> fields) {
        List<int[]> ranges = new ArrayList<>();
        int from = 0;
        int currentChars = 0;
        for (int i = 0; i < fields.size(); i++) {
            int chars = countFieldChars(fields.get(i));
            boolean tooManyFields = (i - from) >= FIELDS_PER_EMBED;
            boolean tooManyChars = currentChars + chars > EMBED_FIELDS_BUDGET;
            if (i > from && (tooManyFields || tooManyChars)) {
                ranges.add(new int[]{from, i});
                from = i;
                currentChars = 0;
            }
            currentChars += chars;
        }
        if (from < fields.size()) {
            ranges.add(new int[]{from, fields.size()});
        }
        return ranges;
    }

    private int countFieldChars(Map<String, Object> field) {
        String name = (String) field.get("name");
        String value = (String) field.get("value");
        return (name == null ? 0 : name.length()) + (value == null ? 0 : value.length());
    }

    private Map<String, Object> buildEmbed(
        DiscordDomainBatchMessage message,
        List<CommentEntry> commentChunk,
        List<Map<String, Object>> fieldChunk,
        int pageIndex,
        int totalPages
    ) {
        Map<String, Object> embed = new LinkedHashMap<>();
        String titleSuffix = totalPages == 1 ? "" : " (" + pageIndex + "/" + totalPages + ")";
        embed.put("title", message.domainKey() + " 관련 새로운 논의사항이 "
            + message.comments().size() + "개 있어요! " + titleSuffix);
        embed.put("color", EMBED_COLOR_FIGMA);
        embed.put("fields", new ArrayList<>(fieldChunk));

        Map<String, Object> footer = new LinkedHashMap<>();
        footer.put("text", buildFooter(message));
        embed.put("footer", footer);

        // 가장 최근 댓글 시각을 embed timestamp 로 노출
        commentChunk.stream()
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
