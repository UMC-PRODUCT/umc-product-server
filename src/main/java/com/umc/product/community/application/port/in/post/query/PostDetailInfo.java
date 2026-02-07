package com.umc.product.community.application.port.in.post.query;

import com.umc.product.community.application.port.in.PostInfo;
import com.umc.product.community.domain.enums.Category;
import java.time.LocalDateTime;

public record PostDetailInfo(
        Long postId,
        String title,
        String content,
        Category category,
        LocalDateTime meetAt,
        String location,
        Integer maxParticipants,
        int commentCount
) {
    public static PostDetailInfo of(PostInfo postInfo, int commentCount) {
        return new PostDetailInfo(
                postInfo.postId(),
                postInfo.title(),
                postInfo.content(),
                postInfo.category(),
                postInfo.meetAt(),
                postInfo.location(),
                postInfo.maxParticipants(),
                commentCount
        );
    }
}
