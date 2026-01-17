package com.umc.product.community.adapter.in.web.dto.response;

import com.umc.product.community.application.port.in.post.ToggleCommentLikeUseCase;
import com.umc.product.community.application.port.in.post.TogglePostLikeUseCase;

public record LikeResponse(
        boolean liked,
        int likeCount
) {
    public static LikeResponse from(TogglePostLikeUseCase.LikeResult result) {
        return new LikeResponse(result.liked(), result.likeCount());
    }

    public static LikeResponse from(ToggleCommentLikeUseCase.LikeResult result) {
        return new LikeResponse(result.liked(), result.likeCount());
    }
}
