package com.umc.product.community.application.port.out;

import com.umc.product.community.application.port.in.post.ToggleCommentLikeUseCase.LikeResult;
import com.umc.product.community.domain.Comment;

public interface SaveCommentPort {
    Comment save(Comment comment);

    void delete(Comment comment);

    LikeResult toggleLike(Long commentId, Long challengerId);
}
