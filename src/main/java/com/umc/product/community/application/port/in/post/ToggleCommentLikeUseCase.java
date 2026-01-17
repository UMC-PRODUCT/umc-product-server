package com.umc.product.community.application.port.in.post;

public interface ToggleCommentLikeUseCase {

    LikeResult toggle(Long commentId, Long challengerId);

    record LikeResult(
            boolean liked,
            int likeCount
    ) {}
}
