package com.umc.product.community.application.port.in;

import com.umc.product.community.domain.Post;
import com.umc.product.community.domain.enums.Category;
import java.time.LocalDateTime;

public record PostInfo(
        Long postId,
        String title,
        String content,
        Category category,
        String region,
        boolean anonymous,
        LocalDateTime meetAt,
        String location,
        Integer maxParticipants
) {
    public static PostInfo from(Post post) {
        Long postId = post.getPostId() != null ? post.getPostId().id() : null;

        // 번개글인 경우
        if (post.isLightning()) {
            Post.LightningInfo info = post.getLightningInfoOrThrow();
            return new PostInfo(
                    postId,
                    post.getTitle(),
                    post.getContent(),
                    post.getCategory(),
                    post.getRegion(),
                    post.isAnonymous(),
                    info.meetAt(),
                    info.location(),
                    info.maxParticipants()
            );
        }

        // 일반 게시글
        return new PostInfo(
                postId,
                post.getTitle(),
                post.getContent(),
                post.getCategory(),
                post.getRegion(),
                post.isAnonymous(),
                null,
                null,
                null
        );
    }
}
