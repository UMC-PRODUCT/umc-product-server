package com.umc.product.community.application.port.in.post;

import com.umc.product.community.domain.Comment;
import java.time.Instant;

public record CommentInfo(
        Long commentId,
        Long postId,
        Long challengerId,
        String challengerName,
        String content,
        Instant createdAt
) {
    public static CommentInfo from(Comment comment, String challengerName) {
        Long id = comment.getCommentId() != null ? comment.getCommentId().id() : null;
        return new CommentInfo(
                id,
                comment.getPostId(),
                comment.getChallengerId(),
                challengerName,
                comment.getContent(),
                comment.getCreatedAt()
        );
    }
}
