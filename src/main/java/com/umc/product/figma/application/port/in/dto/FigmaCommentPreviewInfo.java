package com.umc.product.figma.application.port.in.dto;

import java.time.Instant;
import java.util.List;

/**
 * Preview 시 sync 상태를 변경하지 않고 "지금 sync 하면 어떻게 처리될지" 를 그대로 반환한다.
 */
public record FigmaCommentPreviewInfo(
    String fileKey,
    String displayName,
    String lastSyncedCommentId,
    Instant lastSyncedAt,
    int newCommentCount,
    List<Item> items
) {
    public record Item(
        String commentId,
        String message,
        String authorName,
        String nodeId,
        String pageName,
        String classifiedDomainKey,
        String appliedDomainKey,
        String appliedWebhookUrl,
        List<String> mentionRenders,
        boolean fallback,
        boolean unmatched
    ) {
    }
}
