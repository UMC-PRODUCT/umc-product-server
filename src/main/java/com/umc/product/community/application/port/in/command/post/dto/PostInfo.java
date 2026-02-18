package com.umc.product.community.application.port.in.command.post.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.community.domain.Post;
import com.umc.product.community.domain.enums.Category;
import java.time.Instant;
import java.time.LocalDateTime;

public record PostInfo(
    Long postId,
    String title,
    String content,
    Category category,
    Long authorId, // Author Challenger ID임 주의
    String authorName,
    String authorProfileImage,
    ChallengerPart authorPart,
    LocalDateTime meetAt,
    String location,
    Integer maxParticipants,
    String openChatUrl,
    Instant createdAt,
    int commentCount,
    int likeCount,
    boolean isLiked,
    boolean isAuthor
) {
    public static PostInfo from(Post post, Long authorId, String authorName) {
        return from(post, authorId, authorName, null, null, 0, false);
    }

    public static PostInfo from(Post post, Long authorId, String authorName, int commentCount) {
        return from(post, authorId, authorName, null, null, commentCount, false);
    }

    public static PostInfo from(Post post, Long authorId, String authorName, String authorProfileImage,
                                int commentCount) {
        return from(post, authorId, authorName, authorProfileImage, null, commentCount, false);
    }

    public static PostInfo from(Post post, Long authorId, String authorName, String authorProfileImage,
                                ChallengerPart authorPart, int commentCount) {
        return from(post, authorId, authorName, authorProfileImage, authorPart, commentCount, false);
    }

    public static PostInfo from(Post post, Long authorId, String authorName, String authorProfileImage,
                                ChallengerPart authorPart, int commentCount, boolean isAuthor) {
        Long postId = post.getPostId() != null ? post.getPostId().id() : null;

        // 번개글인 경우
        if (post.isLightning()) {
            Post.LightningInfo info = post.getLightningInfoOrThrow();
            return new PostInfo(
                postId,
                post.getTitle(),
                post.getContent(),
                post.getCategory(),
                authorId,
                authorName,
                authorProfileImage,
                authorPart,
                info.meetAt(),
                info.location(),
                info.maxParticipants(),
                info.openChatUrl(),
                post.getCreatedAt(),
                commentCount,
                post.getLikeCount(),
                post.isLiked(),
                isAuthor
            );
        }

        // 일반 게시글
        return new PostInfo(
            postId,
            post.getTitle(),
            post.getContent(),
            post.getCategory(),
            authorId,
            authorName,
            authorProfileImage,
            authorPart,
            null,
            null,
            null,
            null,
            post.getCreatedAt(),
            commentCount,
            post.getLikeCount(),
            post.isLiked(),
            isAuthor
        );
    }
}
