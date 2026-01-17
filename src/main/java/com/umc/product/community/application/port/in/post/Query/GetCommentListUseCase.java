package com.umc.product.community.application.port.in.post.Query;

import com.umc.product.community.application.port.in.post.CommentInfo;
import java.util.List;

public interface GetCommentListUseCase {
    List<CommentInfo> getComments(Long postId);
}
