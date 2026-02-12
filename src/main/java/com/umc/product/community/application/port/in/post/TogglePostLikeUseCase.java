package com.umc.product.community.application.port.in.post;

public interface TogglePostLikeUseCase {

    LikeResult toggleLike(Long postId, Long challengerId);

    record LikeResult(
            boolean liked,
            int likeCount
    ) {}
}
