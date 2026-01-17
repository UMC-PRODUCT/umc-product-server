package com.umc.product.community.application.port.in.post;

public interface TogglePostLikeUseCase {

    LikeResult toggle(Long postId, Long challengerId);

    record LikeResult(
            boolean liked,
            int likeCount
    ) {}
}
