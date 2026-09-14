package com.umc.product.community.application.port.in.command.comment;

public interface DeleteCommentUseCase {
    void delete(Long commentId, Long challengerId);
}
