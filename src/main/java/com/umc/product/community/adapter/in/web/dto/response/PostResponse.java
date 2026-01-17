package com.umc.product.community.adapter.in.web.dto.response;

import com.umc.product.community.application.port.in.PostInfo;
import com.umc.product.community.domain.enums.Category;
import java.time.LocalDateTime;

public record PostResponse(
        Long postId,
        String title,
        String content,
        Category category,
        String region,
        boolean anonymous,
        LightningInfoResponse lightningInfo
) {
    public static PostResponse from(PostInfo info) {
        LightningInfoResponse lightningInfoResponse = null;

        if (info.category() == Category.LIGHTNING) {
            lightningInfoResponse = new LightningInfoResponse(
                    info.meetAt(),
                    info.location(),
                    info.maxParticipants()
            );
        }

        return new PostResponse(
                info.postId(),
                info.title(),
                info.content(),
                info.category(),
                info.region(),
                info.anonymous(),
                lightningInfoResponse
        );
    }

    public record LightningInfoResponse(
            LocalDateTime meetAt,
            String location,
            Integer maxParticipants
    ) {
    }
}
