package com.umc.product.figma.application.port.out.dto;

import java.time.Instant;

/**
 * Figma REST의 댓글을 도메인 입력으로 변환한 형태.
 */
public record FigmaCommentInfo(
    String commentId,
    String message,
    String authorName,
    String nodeId,
    Instant createdAt
) {
}
