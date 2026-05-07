package com.umc.product.figma.adapter.out.external;

import com.umc.product.figma.application.port.out.SendDiscordMentionPort;
import com.umc.product.figma.application.port.out.dto.DiscordDomainBatchMessage;
import com.umc.product.figma.application.port.out.dto.DiscordDomainBatchMessage.CommentEntry;
import com.umc.product.figma.domain.exception.FigmaDomainException;
import com.umc.product.figma.domain.exception.FigmaErrorCode;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * 도메인 단위로 묶인 댓글 batch 를 Discord 로 발송하는 webhook 어댑터.
 *
 * <p>발송 흐름은 다음 6단계로 분리되어 있다.
 * <ol>
 *   <li>{@link #buildFields} — 댓글 1건 → field 1개 (name/value 자르고 GAP 추가)</li>
 *   <li>{@link #packFieldsIntoChunks} — fields 25개 + payload size budget 두 제약으로 embed 경계 결정</li>
 *   <li>{@link #repackOversizedChunks} — 빌드 후 실측 size 가 한도를 넘는 embed 를 절반씩 재귀 분할</li>
 *   <li>{@link #paginateEmbeds} — embed 10개 한도 + 페이지 합산 size 한도로 메시지 단위 페이지네이션</li>
 *   <li>{@link #ensureWithinDiscordLimit} — 발송 직전 최종 단언, 한도 초과 payload 의 Discord 호출 차단</li>
 *   <li>{@link #sendPages} — 페이지마다 webhook POST. 첫 메시지 content 에만 멘션 포함</li>
 * </ol>
 *
 * <p>Discord embed 제약:
 * <ul>
 *   <li>embed 1건당 fields 최대 25</li>
 *   <li>메시지 1건당 embeds 최대 10</li>
 *   <li>field name ≤ 256 char, field value ≤ 1024 char (per-field 는 char 기준)</li>
 *   <li><b>한 메시지의 모든 embed 합산 size ≤ 6000</b> ("across all embeds in a message").
 *       단일 embed 가 6000 미만이어도 한 메시지에 여러 embed 가 들어가 합산이 6000 을 넘으면 reject 된다.</li>
 * </ul>
 *
 * <p><b>합산 size 측정 단위:</b> Discord 는 6000 한도의 카운트 단위를 명세에 명시하지 않는다.
 * Java {@code length()} 와 차이가 발생하는 가장 큰 후보는 UTF-8 byte 길이(한국어 1자 = 3 byte,
 * 이모지 1자 = 4 byte) 이므로, packing 과 검증 모두 <b>UTF-8 byte 기준</b>으로 측정한다.
 * Discord 가 char 기준이라면 이 측정은 보수적이라 안전하고, byte 기준이라면 정확하다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DiscordMentionWebhookAdapter implements SendDiscordMentionPort {

    /* ===== Discord 명세 한도 ===== */

    private static final int FIELDS_PER_EMBED = 25;
    private static final int EMBEDS_PER_MESSAGE = 10;
    private static final int FIELD_NAME_MAX_CHARS = 256;
    private static final int FIELD_VALUE_MAX_CHARS = 1024;
    /** embed 한 건의 모든 텍스트 필드 합산 한도. 초과 시 400 BAD_REQUEST. */
    private static final int EMBED_TOTAL_SIZE_MAX = 6000;

    /* ===== 우리 packing 마진 ===== */

    /**
     * Title / footer / 분할 표기 등 fields 외 영역의 가변 길이를 흡수하는 마진.
     * packing 은 (6000 − 500 = 5500) 까지만 사용한다.
     */
    private static final int EMBED_SAFETY_MARGIN = 500;
    private static final int EMBED_USABLE_SIZE = EMBED_TOTAL_SIZE_MAX - EMBED_SAFETY_MARGIN;

    /* ===== 렌더링 상수 ===== */

    private static final int EMBED_COLOR_FIGMA = 0x5DADE2;
    /**
     * 댓글 사이 시각적 여백. Discord 가 non-inline field 들을 거의 붙여 렌더링해 가독성이 떨어지므로
     * 마지막 줄 다음에 빈 줄 한 칸을 강제한다 (field 수에 영향 없음).
     */
    private static final String FIELD_VALUE_TRAILING_GAP = "\n​";
    private static final ZoneId FOOTER_ZONE = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter FOOTER_FORMAT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(FOOTER_ZONE);
    private static final String FOOTER_ZONE_LABEL = "KST";

    private final RestClient restClient;

    /* ===================================================================== */
    /*                              Public API                               */
    /* ===================================================================== */

    @Override
    public void send(DiscordDomainBatchMessage message) {
        if (message.comments() == null || message.comments().isEmpty()) {
            return;
        }

        List<Map<String, Object>> embeds = buildEmbeds(message);
        List<List<Map<String, Object>>> pages = paginateEmbeds(embeds);
        ensureWithinDiscordLimit(message.domainKey(), pages);
        sendPages(message, renderMentionLine(message.mentionRenders()), pages);
    }

    /* ===================================================================== */
    /*                        1. 빌드 (comments → embeds)                     */
    /* ===================================================================== */

    private List<Map<String, Object>> buildEmbeds(DiscordDomainBatchMessage message) {
        List<CommentEntry> comments = message.comments();
        List<Map<String, Object>> fields = buildFields(comments);

        List<int[]> chunks = packFieldsIntoChunks(fields, message);
        chunks = repackOversizedChunks(chunks, comments, fields, message);

        int total = chunks.size();
        List<Map<String, Object>> embeds = new ArrayList<>(total);
        for (int i = 0; i < total; i++) {
            int from = chunks.get(i)[0];
            int to = chunks.get(i)[1];
            embeds.add(buildEmbed(
                message,
                comments.subList(from, to),
                fields.subList(from, to),
                i + 1,
                total
            ));
        }
        return embeds;
    }

    private List<Map<String, Object>> buildFields(List<CommentEntry> comments) {
        List<Map<String, Object>> fields = new ArrayList<>(comments.size());
        for (CommentEntry c : comments) {
            fields.add(buildField(c));
        }
        return fields;
    }

    private Map<String, Object> buildField(CommentEntry c) {
        String pageSuffix = (c.pageName() == null || c.pageName().isBlank()) ? "" : " / " + c.pageName();
        String name = truncateChars(
            "🌸 " + safe(c.authorName()) + " · " + safe(c.fileDisplayName()) + pageSuffix,
            FIELD_NAME_MAX_CHARS
        );
        String body = truncateChars(
            safe(c.message()) + "\n🔗 [바로가기](" + safe(c.commentLink()) + ")",
            FIELD_VALUE_MAX_CHARS - FIELD_VALUE_TRAILING_GAP.length()
        );
        String value = body + FIELD_VALUE_TRAILING_GAP;

        Map<String, Object> field = new LinkedHashMap<>();
        field.put("name", name);
        field.put("value", value);
        field.put("inline", false);
        return field;
    }

    private Map<String, Object> buildEmbed(
        DiscordDomainBatchMessage message,
        List<CommentEntry> commentChunk,
        List<Map<String, Object>> fieldChunk,
        int pageIndex,
        int totalPages
    ) {
        Map<String, Object> embed = new LinkedHashMap<>();
        embed.put("title", buildEmbedTitle(message, totalPages, pageIndex));
        embed.put("color", EMBED_COLOR_FIGMA);
        embed.put("fields", new ArrayList<>(fieldChunk));

        Map<String, Object> footer = new LinkedHashMap<>();
        footer.put("text", buildFooterText(message));
        embed.put("footer", footer);

        commentChunk.stream()
            .map(CommentEntry::createdAt)
            .filter(Objects::nonNull)
            .max(Instant::compareTo)
            .ifPresent(t -> embed.put("timestamp", t.toString()));

        return embed;
    }

    private String buildEmbedTitle(DiscordDomainBatchMessage message, int totalPages, int pageIndex) {
        String suffix = (totalPages <= 1) ? "" : " (" + pageIndex + "/" + totalPages + ")";
        return message.domainKey()
            + " 관련 새로운 논의사항이 "
            + message.comments().size()
            + "개 있어요!"
            + suffix;
    }

    private String buildFooterText(DiscordDomainBatchMessage message) {
        if (message.windowFrom() == null && message.windowTo() == null) {
            return "Figma comment forwarder";
        }
        String from = message.windowFrom() == null ? "-" : FOOTER_FORMAT.format(message.windowFrom());
        String to = message.windowTo() == null ? "-" : FOOTER_FORMAT.format(message.windowTo());
        return "Figma · " + from + " ~ " + to + " " + FOOTER_ZONE_LABEL;
    }

    /* ===================================================================== */
    /*                        2. Chunking (greedy packing)                    */
    /* ===================================================================== */

    /**
     * fields 를 두 가지 한도 안에서 greedy 로 chunk 분할한다.
     * <ul>
     *   <li>한 chunk 의 fields 수 ≤ {@link #FIELDS_PER_EMBED}</li>
     *   <li>한 chunk 의 fields name+value UTF-8 byte 합 ≤
     *       {@link #EMBED_USABLE_SIZE} − 추정 fixedOverhead</li>
     * </ul>
     */
    private List<int[]> packFieldsIntoChunks(
        List<Map<String, Object>> fields,
        DiscordDomainBatchMessage message
    ) {
        int fixedOverhead = estimateFixedOverhead(message);
        // 한 field 의 최대 byte (name 256 char × 4 + value 1024 char × 4 = 5120 byte).
        // budget 이 단일 field 보다 작아질 일은 없게 lower bound 를 둔다.
        int singleFieldMaxBytes = (FIELD_NAME_MAX_CHARS + FIELD_VALUE_MAX_CHARS) * 4;
        int fieldsBudget = Math.max(EMBED_USABLE_SIZE - fixedOverhead, singleFieldMaxBytes);

        List<int[]> chunks = new ArrayList<>();
        int from = 0;
        int currentSize = 0;
        for (int i = 0; i < fields.size(); i++) {
            int fieldSize = byteSizeOfField(fields.get(i));
            boolean reachedFieldsLimit = (i - from) >= FIELDS_PER_EMBED;
            boolean wouldExceedBudget = currentSize + fieldSize > fieldsBudget;
            if (i > from && (reachedFieldsLimit || wouldExceedBudget)) {
                chunks.add(new int[]{from, i});
                from = i;
                currentSize = 0;
            }
            currentSize += fieldSize;
        }
        if (from < fields.size()) {
            chunks.add(new int[]{from, fields.size()});
        }
        return chunks;
    }

    /**
     * title + footer + 분할 표기 등 fields 외 영역의 UTF-8 byte 길이를 보수적으로 추정한다.
     * domainKey 가 길수록(한국어/이모지 포함) byte 수가 늘어나므로 기본값에 더해 실제 length 의 3배를 함께 본다.
     */
    private int estimateFixedOverhead(DiscordDomainBatchMessage message) {
        // "{domainKey} 관련 새로운 논의사항이 N개 있어요! (M/M)" — 한국어 ~25자 × 3 byte + domainKey × 3 + suffix
        int titleEstimate = byteSizeOf(message.domainKey()) + 100;
        // "Figma · YYYY-MM-DD HH:mm ~ YYYY-MM-DD HH:mm KST" — 약 50 char, 거의 ASCII
        int footerEstimate = 80;
        return titleEstimate + footerEstimate;
    }

    /* ===================================================================== */
    /*           3. 빌드 후 실측 검증 + 한도 초과 chunk 자동 재분할             */
    /* ===================================================================== */

    /**
     * 빌드 결과의 실측 byte 합이 6000 한도를 넘는 chunk 는 fields 를 절반씩 재귀 분할한다.
     * fields 가 1개로 줄어도 한도 초과면(buildField 가 char 단위로 자르므로 byte 로는 최대 5120 byte,
     * fixedOverhead 포함해도 5500 byte 미만이라 정상 흐름에서 발생 X) 그대로 두고 발송 직전
     * {@link #ensureWithinDiscordLimit} 가 최종 차단한다.
     */
    private List<int[]> repackOversizedChunks(
        List<int[]> chunks,
        List<CommentEntry> comments,
        List<Map<String, Object>> fields,
        DiscordDomainBatchMessage message
    ) {
        int provisionalTotal = chunks.size();
        List<int[]> verified = new ArrayList<>(provisionalTotal);
        for (int[] chunk : chunks) {
            verified.addAll(splitUntilUnderLimit(
                chunk[0], chunk[1], comments, fields, message, provisionalTotal
            ));
        }
        return verified;
    }

    private List<int[]> splitUntilUnderLimit(
        int from,
        int to,
        List<CommentEntry> comments,
        List<Map<String, Object>> fields,
        DiscordDomainBatchMessage message,
        int provisionalTotal
    ) {
        Map<String, Object> probe = buildEmbed(
            message, comments.subList(from, to), fields.subList(from, to), 1, provisionalTotal
        );
        int size = byteSizeOfEmbed(probe);
        if (size <= EMBED_TOTAL_SIZE_MAX || (to - from) <= 1) {
            if (size > EMBED_TOTAL_SIZE_MAX) {
                log.warn("Discord embed 단일 field 가 한도 초과 (분할 불가). bytes={}, range=[{},{})",
                    size, from, to);
            }
            return List.of(new int[]{from, to});
        }
        int mid = from + (to - from) / 2;
        List<int[]> result = new ArrayList<>();
        result.addAll(splitUntilUnderLimit(from, mid, comments, fields, message, provisionalTotal + 1));
        result.addAll(splitUntilUnderLimit(mid, to, comments, fields, message, provisionalTotal + 1));
        return result;
    }

    /* ===================================================================== */
    /*                4. 페이지네이션 (embed 10개 + size 합산 한도)             */
    /* ===================================================================== */

    /**
     * embeds 를 두 가지 한도 안에서 greedy 로 페이지(=메시지) 단위로 분할한다.
     * <ul>
     *   <li>한 페이지의 embed 수 ≤ {@link #EMBEDS_PER_MESSAGE}</li>
     *   <li>한 페이지의 embeds 합산 UTF-8 byte ≤ {@link #EMBED_USABLE_SIZE}</li>
     * </ul>
     * 두 번째 한도가 본 어댑터의 핵심이다. Discord 6000 한도는 embed 단위가 아니라
     * "한 메시지의 모든 embed 합산" 단위이므로, 단일 embed 검증만으로는 보호되지 않는다.
     */
    private List<List<Map<String, Object>>> paginateEmbeds(List<Map<String, Object>> embeds) {
        List<List<Map<String, Object>>> pages = new ArrayList<>();
        List<Map<String, Object>> currentPage = new ArrayList<>();
        int currentPageSize = 0;

        for (Map<String, Object> embed : embeds) {
            int embedSize = byteSizeOfEmbed(embed);
            boolean reachedEmbedLimit = currentPage.size() >= EMBEDS_PER_MESSAGE;
            boolean wouldExceedSizeLimit = currentPageSize + embedSize > EMBED_USABLE_SIZE;
            if (!currentPage.isEmpty() && (reachedEmbedLimit || wouldExceedSizeLimit)) {
                pages.add(currentPage);
                currentPage = new ArrayList<>();
                currentPageSize = 0;
            }
            currentPage.add(embed);
            currentPageSize += embedSize;
        }
        if (!currentPage.isEmpty()) {
            pages.add(currentPage);
        }
        return pages;
    }

    /* ===================================================================== */
    /*                        5. 발송 직전 최종 단언                           */
    /* ===================================================================== */

    /**
     * 두 가지 한도를 동시에 단언한다.
     * <ol>
     *   <li>단일 embed 의 size ≤ 6000 — {@link #repackOversizedChunks} 가 이미 보장</li>
     *   <li><b>한 페이지(메시지) 안의 embeds 합산 size ≤ 6000</b> — Discord 의 진짜 한도.
     *       {@link #paginateEmbeds} 가 size-aware 로 분할하므로 정상 흐름에서는 통과한다.</li>
     * </ol>
     * 한도 초과 payload 를 그대로 Discord 로 보내 400 BAD_REQUEST 가 발생하는 사고를 사전 차단하는 안전망.
     */
    private void ensureWithinDiscordLimit(String domainKey, List<List<Map<String, Object>>> pages) {
        for (int p = 0; p < pages.size(); p++) {
            List<Map<String, Object>> embeds = pages.get(p);
            int pageSize = 0;
            for (int e = 0; e < embeds.size(); e++) {
                int embedSize = byteSizeOfEmbed(embeds.get(e));
                if (embedSize > EMBED_TOTAL_SIZE_MAX) {
                    log.error("Discord embed 단일 한도 초과: domainKey={}, page={}, embedIdx={}, bytes={}",
                        domainKey, p + 1, e + 1, embedSize);
                    throw new FigmaDomainException(FigmaErrorCode.DISCORD_MENTION_SEND_FAILED);
                }
                pageSize += embedSize;
            }
            if (pageSize > EMBED_TOTAL_SIZE_MAX) {
                log.error("Discord 메시지 합산 한도 초과: domainKey={}, page={}, bytes={}, embeds={}",
                    domainKey, p + 1, pageSize, embeds.size());
                throw new FigmaDomainException(FigmaErrorCode.DISCORD_MENTION_SEND_FAILED);
            }
        }
    }

    /* ===================================================================== */
    /*                        6. 발송 (webhook POST)                          */
    /* ===================================================================== */

    private void sendPages(
        DiscordDomainBatchMessage message,
        String mentionLine,
        List<List<Map<String, Object>>> pages
    ) {
        for (int i = 0; i < pages.size(); i++) {
            boolean firstMessage = (i == 0);
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("content", firstMessage ? mentionLine : "");
            payload.put("embeds", pages.get(i));
            payload.put("allowed_mentions", Map.of("parse", List.of("roles", "users")));

            try {
                restClient.post()
                    .uri(message.webhookUrl())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
                log.debug("Discord domain batch 전송 완료: domainKey={}, page={}/{}, comments={}",
                    message.domainKey(), i + 1, pages.size(), message.comments().size());
            } catch (RestClientResponseException e) {
                log.error("Discord domain batch 전송 실패: domainKey={}, page={}/{}, status={}, body={}",
                    message.domainKey(), i + 1, pages.size(),
                    e.getStatusCode(), e.getResponseBodyAsString());
                throw new FigmaDomainException(FigmaErrorCode.DISCORD_MENTION_SEND_FAILED);
            }
        }
    }

    /* ===================================================================== */
    /*                              헬퍼                                     */
    /* ===================================================================== */

    private String renderMentionLine(List<String> mentionRenders) {
        if (mentionRenders == null || mentionRenders.isEmpty()) {
            return "";
        }
        return String.join(" ", mentionRenders);
    }

    private static int byteSizeOfField(Map<String, Object> field) {
        return byteSizeOf((String) field.get("name")) + byteSizeOf((String) field.get("value"));
    }

    /**
     * Discord 6000 한도에 합산되는 embed 텍스트 필드의 총 UTF-8 byte 수.
     * timestamp / color / image / thumbnail 은 한도에 포함되지 않으므로 제외한다.
     */
    private static int byteSizeOfEmbed(Map<String, Object> embed) {
        int total = 0;
        total += byteSizeOf((String) embed.get("title"));
        total += byteSizeOf((String) embed.get("description"));
        if (embed.get("author") instanceof Map<?, ?> author) {
            total += byteSizeOf((String) author.get("name"));
        }
        if (embed.get("footer") instanceof Map<?, ?> footer) {
            total += byteSizeOf((String) footer.get("text"));
        }
        if (embed.get("fields") instanceof List<?> list) {
            for (Object o : list) {
                if (o instanceof Map<?, ?> field) {
                    total += byteSizeOf((String) field.get("name"));
                    total += byteSizeOf((String) field.get("value"));
                }
            }
        }
        return total;
    }

    private static int byteSizeOf(String s) {
        return s == null ? 0 : s.getBytes(StandardCharsets.UTF_8).length;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    /**
     * Discord 의 per-field char 한도(name 256, value 1024)에 맞춰 char 수 기준으로 자른다.
     * 6000 합산 한도는 별도로 byte 기준으로 검증한다.
     */
    private static String truncateChars(String value, int max) {
        if (value == null) {
            return "";
        }
        if (value.length() <= max) {
            return value;
        }
        return value.substring(0, max - 1) + "…";
    }
}
