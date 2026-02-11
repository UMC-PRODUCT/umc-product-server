package com.umc.product.community.application.port.in;

import com.umc.product.community.domain.Post;
import com.umc.product.community.domain.enums.Category;
import java.time.Instant;
import java.time.LocalDateTime;

public record PostInfo(
        Long postId,
        String title,
        String content,
        Category category,
        Long authorId,
        String authorName,
        LocalDateTime meetAt,
        String location,
        Integer maxParticipants,
        String openChatUrl,
        Instant createdAt,
        int likeCount,
        boolean isLiked
) {
    public static PostInfo from(Post post, Long authorId, String authorName) {
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
                    info.meetAt(),
                    info.location(),
                    info.maxParticipants(),
                    info.openChatUrl(),
                    post.getCreatedAt(),
                    post.getLikeCount(),
                    post.isLiked()
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
                null,
                null,
                null,
                null,
                post.getCreatedAt(),
                post.getLikeCount(),
                post.isLiked()
        );
    }
}
