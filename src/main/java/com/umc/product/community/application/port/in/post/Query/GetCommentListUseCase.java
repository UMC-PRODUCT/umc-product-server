package com.umc.product.community.application.port.in.post.Query;

import com.umc.product.community.application.port.in.post.CommentInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GetCommentListUseCase {
    Page<CommentInfo> getComments(Long postId, Pageable pageable);
}
