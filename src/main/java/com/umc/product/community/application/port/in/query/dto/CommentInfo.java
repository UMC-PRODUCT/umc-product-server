package com.umc.product.community.application.port.in.query.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.community.domain.Comment;
import java.time.Instant;

public record CommentInfo(
    Long commentId,
    Long postId,
    Long challengerId,
    String challengerName,
    String challengerProfileImage,
    ChallengerPart challengerPart,
    String content,
    Instant createdAt,
    boolean isAuthor
) {
    public static CommentInfo from(Comment comment, String challengerName) {
        return from(comment, challengerName, null, null, false);
    }

    public static CommentInfo from(Comment comment, String challengerName, String challengerProfileImage) {
        return from(comment, challengerName, challengerProfileImage, null, false);
    }

    public static CommentInfo from(Comment comment, String challengerName, String challengerProfileImage,
                                   ChallengerPart challengerPart) {
        return from(comment, challengerName, challengerProfileImage, challengerPart, false);
    }

    public static CommentInfo from(Comment comment, String challengerName, String challengerProfileImage,
                                   ChallengerPart challengerPart, boolean isAuthor) {
        Long id = comment.getCommentId() != null ? comment.getCommentId().id() : null;
        return new CommentInfo(
            id,
            comment.getPostId(),
            comment.getChallengerId(),
            challengerName,
            challengerProfileImage,
            challengerPart,
            comment.getContent(),
            comment.getCreatedAt(),
            isAuthor
        );
    }
}
