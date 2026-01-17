package com.umc.product.community.application.port.in.post;

public interface DeleteCommentUseCase {
    void delete(Long commentId, Long challengerId);
}
