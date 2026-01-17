package com.umc.product.community.application.port.in.post.Query;

import com.umc.product.community.application.port.in.PostInfo;
import com.umc.product.community.domain.enums.Category;
import java.time.LocalDateTime;

public record PostDetailInfo(
        Long postId,
        String title,
        String content,
        Category category,
        String region,
        boolean anonymous,
        LocalDateTime meetAt,
        String location,
        Integer maxParticipants,
        // int likeCount,
        // boolean isLiked,
        int commentCount
) {
    public static PostDetailInfo of(PostInfo postInfo, /* int likeCount, boolean isLiked, */ int commentCount) {
        return new PostDetailInfo(
                postInfo.postId(),
                postInfo.title(),
                postInfo.content(),
                postInfo.category(),
                postInfo.region(),
                postInfo.anonymous(),
                postInfo.meetAt(),
                postInfo.location(),
                postInfo.maxParticipants(),
                // likeCount,
                // isLiked,
                commentCount
        );
    }
}
