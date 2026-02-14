package com.umc.product.community.application.port.in.post;

import com.umc.product.community.domain.Comment;
import java.time.Instant;

public record CommentInfo(
        Long commentId,
        Long postId,
        Long challengerId,
        String challengerName,
        String challengerProfileImage,
        String content,
        Instant createdAt
) {
    public static CommentInfo from(Comment comment, String challengerName) {
        return from(comment, challengerName, null);
    }

    public static CommentInfo from(Comment comment, String challengerName, String challengerProfileImage) {
        Long id = comment.getCommentId() != null ? comment.getCommentId().id() : null;
        return new CommentInfo(
                id,
                comment.getPostId(),
                comment.getChallengerId(),
                challengerName,
                challengerProfileImage,
                comment.getContent(),
                comment.getCreatedAt()
        );
    }
}
