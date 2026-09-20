package com.umc.product.community.application.port.in.query;

import java.util.List;

import com.umc.product.community.application.port.in.query.dto.CommentInfo;

public interface GetCommentListUseCase {
    List<CommentInfo> getComments(Long postId);

    List<CommentInfo> getComments(Long postId, Long challengerId);

    CommentInfo getComment(Long commentId);
}
