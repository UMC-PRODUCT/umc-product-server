package com.umc.product.report.adapter.out.delivery.discord;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.umc.product.report.adapter.discord.DiscordText;
import com.umc.product.report.adapter.discord.ReportDiscordButtons;
import com.umc.product.report.domain.Report;
import com.umc.product.report.domain.ReportTargetSnapshot;
import com.umc.product.report.domain.enums.ReportReason;
import com.umc.product.report.domain.enums.ReportTargetType;

/**
 * 신고를 Discord 메시지 본문으로 바꿉니다.
 */
@Component
public class DiscordReportMessageFactory {

    static final int CONTENT_PREVIEW_MAX_LENGTH = 200;
    private static final ZoneId DISPLAY_ZONE = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter DISPLAY_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public Map<String, Object> create(Report report) {
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("embeds", List.of(embed(report)));
        message.put("components", ReportDiscordButtons.actionRows(report.getId(), false));
        // 신고 내용에 @everyone 등이 있어도 아무도 호출되지 않도록 멘션을 막습니다.
        message.put("allowed_mentions", Map.of("parse", List.of()));
        return message;
    }

    private Map<String, Object> embed(Report report) {
        List<Map<String, Object>> fields = new ArrayList<>();
        fields.add(field("신고 ID", String.valueOf(report.getId()), true));
        fields.add(field("사유", reasonLabel(report.getReasonCode()), true));
        fields.add(field("대상", targetLabel(report), true));

        ReportTargetSnapshot snapshot = report.getTargetSnapshot();
        if (snapshot != null) {
            // Discord는 빈 값의 항목을 거절하므로 내용이 없는 위치는 넣지 않습니다.
            if (snapshot.getContext() != null && !snapshot.getContext().isBlank()) {
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
        return embed;
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
        return authorId == null ? "(작성자 없음)" : String.valueOf(authorId);
    }

    private static String preview(String content) {
        if (content == null || content.isBlank()) {
            return "(내용 없음)";
        }
        return DiscordText.truncate(content, CONTENT_PREVIEW_MAX_LENGTH, "…");
    }
}
