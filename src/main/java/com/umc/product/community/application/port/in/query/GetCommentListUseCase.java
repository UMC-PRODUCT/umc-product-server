package com.umc.product.community.application.port.in.query;

import com.umc.product.community.application.port.in.command.comment.dto.CommentInfo;
import java.util.List;

public interface GetCommentListUseCase {
    List<CommentInfo> getComments(Long postId);

    List<CommentInfo> getComments(Long postId, Long challengerId);

    CommentInfo getComment(Long commentId);
}
