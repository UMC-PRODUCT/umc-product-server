package com.umc.product.report.adapter.out.external;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.umc.product.global.logging.ExternalApiCallLogger;
import com.umc.product.report.application.port.out.ReportDeliveryPort;
import com.umc.product.report.domain.Report;
import com.umc.product.report.domain.ReportTargetSnapshot;
import com.umc.product.report.domain.enums.ReportReason;
import com.umc.product.report.domain.enums.ReportTargetType;

/**
 * 신고를 Discord 신고 채널 웹훅으로 보냅니다.
 * <p>
 * 웹훅 URL({@code REPORT_DISCORD_WEBHOOK_URL})이 비어 있으면 {@link #isEnabled()}가 {@code false}를 돌려주고,
 * 신고는 전달 대기 상태로 남습니다.
 */
@Component
public class DiscordReportDeliveryAdapter implements ReportDeliveryPort {

    static final int CONTENT_PREVIEW_MAX_LENGTH = 200;
    private static final ZoneId DISPLAY_ZONE = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter DISPLAY_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final String GUIDE = "처리 후 이 메시지에 답글로 조치 내용(처리/반려, 사유)을 남겨주세요.";

    private final RestClient restClient;
    private final String webhookUrl;

    public DiscordReportDeliveryAdapter(
        RestClient restClient,
        @Value("${app.report.discord.webhook-url:}") String webhookUrl
    ) {
        this.restClient = restClient;
        this.webhookUrl = webhookUrl;
    }

    @Override
    public boolean isEnabled() {
        return webhookUrl != null && !webhookUrl.isBlank();
    }

    @Override
    public void deliver(Report report) {
        if (!isEnabled()) {
            throw new IllegalStateException("신고 Discord 웹훅이 설정되지 않았습니다.");
        }
        Map<String, Object> payload = payload(report);
        ExternalApiCallLogger.measure("DISCORD", "SEND_REPORT", () ->
            restClient.post()
                .uri(webhookUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .toBodilessEntity()
        );
    }

    Map<String, Object> payload(Report report) {
        List<Map<String, Object>> fields = new ArrayList<>();
        fields.add(field("신고 ID", String.valueOf(report.getId()), true));
        fields.add(field("사유", reasonLabel(report.getReasonCode()), true));
        fields.add(field("대상", targetLabel(report), true));

        ReportTargetSnapshot snapshot = report.getTargetSnapshot();
        if (snapshot != null) {
            if (snapshot.getContext() != null) {
                fields.add(field("위치", snapshot.getContext(), true));
            }
            fields.add(field("작성자 member ID", authorLabel(snapshot.getAuthorId()), true));
            fields.add(field("내용", preview(snapshot.getContent()), false));
        }
        if (report.getCreatedAt() != null) {
            fields.add(field("접수 시각", DISPLAY_TIME.format(report.getCreatedAt().atZone(DISPLAY_ZONE)), true));
        }

        Map<String, Object> embed = new LinkedHashMap<>();
        embed.put("title", "신고 접수 #" + report.getId());
        embed.put("fields", fields);
        embed.put("footer", Map.of("text", GUIDE));
        return Map.of("embeds", List.of(embed));
    }

    private static Map<String, Object> field(String name, String value, boolean inline) {
        return Map.of("name", name, "value", value, "inline", inline);
    }

    private static String targetLabel(Report report) {
        String label = switch (report.getTargetType()) {
            case POST -> "게시글";
            case COMMENT -> "댓글";
            case THREAD_MESSAGE -> "스레드 메시지";
        };
        String target = label + " #" + report.getTargetId();
        if (report.getTargetType() == ReportTargetType.THREAD_MESSAGE && report.getThreadId() != null) {
            return target + " (스레드 #" + report.getThreadId() + ")";
        }
        return target;
    }

    private static String reasonLabel(ReportReason reason) {
        if (reason == null) {
            return "사유 없음";
        }
        return switch (reason) {
            case SPAM -> "스팸";
            case ABUSE -> "욕설/비방";
            case INAPPROPRIATE -> "부적절한 내용";
            case PRIVACY -> "개인정보";
            case ETC -> "기타";
        };
    }

    private static String authorLabel(Long authorId) {
        return authorId == null ? "없음 (시스템 메시지)" : String.valueOf(authorId);
    }

    private static String preview(String content) {
        if (content == null || content.isBlank()) {
            return "(내용 없음)";
        }
        if (content.codePointCount(0, content.length()) <= CONTENT_PREVIEW_MAX_LENGTH) {
            return content;
        }
        // 이모지처럼 두 개의 char로 이루어진 글자가 중간에서 잘리지 않도록 code point 기준으로 자릅니다.
        return content.substring(0, content.offsetByCodePoints(0, CONTENT_PREVIEW_MAX_LENGTH)) + "…";
    }
}
