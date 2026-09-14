package com.umc.product.blog.application.port.out;

import com.umc.product.blog.domain.BlogCommentLike;
import com.umc.product.blog.domain.BlogContentLike;

public interface SaveBlogLikePort {

    BlogContentLike saveContentLike(BlogContentLike like);

    void deleteContentLike(Long contentId, Long memberId);

    BlogCommentLike saveCommentLike(BlogCommentLike like);

    void deleteCommentLike(Long commentId, Long memberId);

    void deleteCommentLikesByCommentId(Long commentId);
}
