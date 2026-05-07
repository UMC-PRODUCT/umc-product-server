package com.umc.product.figma.application.port.in.dto;

import java.time.Instant;
import java.util.List;

/**
 * Preview 시 sync 상태를 변경하지 않고 "지금 sync 하면 어떤 댓글이 어떤 도메인으로 묶여 갈지" 를 sync 와 동일한 grouping 형태로 반환한다 (도메인 → 댓글 묶음).
 * <p>
 * Discord 발송은 일어나지 않는다.
 */
public record FigmaCommentPreviewInfo(
    String fileKey,
    String displayName,
    String lastSyncedCommentId,
    Instant lastSyncedAt,
    int totalComments,
    int unmatchedCount,
    List<DomainGroup> domains
) {
    public record DomainGroup(
        String domainKey,
        String webhookUrl,
        boolean fallback,
        List<String> mentionRenders,
        List<Comment> comments
    ) {
    }

    public record Comment(
        String commentId,
        String message,
        String authorName,
        String nodeId,
        String pageName,
        String classifiedDomainKey,
        Instant createdAt
    ) {
    }
}
