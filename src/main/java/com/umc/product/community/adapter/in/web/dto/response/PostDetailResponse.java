package com.umc.product.community.adapter.in.web.dto.response;

import com.umc.product.community.application.port.in.post.Query.PostDetailInfo;
import com.umc.product.community.domain.enums.Category;
import java.time.LocalDateTime;

public record PostDetailResponse(
        Long postId,
        String title,
        String content,
        Category category,
        String region,
        boolean anonymous,
        LightningInfoResponse lightningInfo,
        //int likeCount,
        //boolean isLiked,
        int commentCount
) {
    public static PostDetailResponse from(PostDetailInfo info) {
        LightningInfoResponse lightningInfoResponse = null;

        if (info.category() == Category.LIGHTNING) {
            lightningInfoResponse = new LightningInfoResponse(
                    info.meetAt(),
                    info.location(),
                    info.maxParticipants()
            );
        }

        return new PostDetailResponse(
                info.postId(),
                info.title(),
                info.content(),
                info.category(),
                info.region(),
                info.anonymous(),
                lightningInfoResponse,
                info.commentCount()
        );
    }

    public record LightningInfoResponse(
            LocalDateTime meetAt,
            String location,
            Integer maxParticipants
    ) {
    }
}
