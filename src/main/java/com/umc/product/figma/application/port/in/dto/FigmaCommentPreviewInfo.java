package com.umc.product.figma.application.port.in.dto;

import java.time.Instant;
import java.util.List;

/**
 * Preview 시 sync 상태를 변경하지 않고 "지금 sync 하면 어떤 댓글이 어떤 도메인으로 묶여 갈지" 를 sync 와 동일한 grouping 형태로 반환한다 (도메인 → 댓글 묶음).
 * <p>
 * Discord 발송은 일어나지 않는다. ADR-004 시점에 시간창 시맨틱으로 옮긴 뒤로는 "최근 N분의 댓글" 을 보여준다 (자세한 시간창 기본값은
 * {@code FigmaCommentPreviewQueryService} 참조).
 */
public record FigmaCommentPreviewInfo(
    String fileKey,
    String displayName,
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

    /**
     * @param alreadyDispatched 본 댓글이 이미 figma_comment_dispatch 에 기록된 (= 과거에 발송된) 댓글인지. true 면
     *                          force=false 인 다음 sync 에서는 발송 대상에서 제외된다.
     */
    public record Comment(
        String commentId,
        String message,
        String authorName,
        String nodeId,
        String pageName,
        String classifiedDomainKey,
        Instant createdAt,
        boolean alreadyDispatched
    ) {
    }
}
