package com.umc.product.community.application.port.in.post.query;

import com.umc.product.community.application.port.in.post.CommentInfo;
import java.util.List;

public interface GetCommentListUseCase {
    List<CommentInfo> getComments(Long postId);
    List<CommentInfo> getComments(Long postId, Long challengerId);
}
