package com.umc.product.techblog.application.port.out;

import com.umc.product.techblog.domain.TechBlogCommentLike;
import com.umc.product.techblog.domain.TechBlogContentLike;

public interface SaveTechBlogLikePort {

    TechBlogContentLike saveContentLike(TechBlogContentLike like);

    void deleteContentLike(Long contentId, Long memberId);

    TechBlogCommentLike saveCommentLike(TechBlogCommentLike like);

    void deleteCommentLike(Long commentId, Long memberId);

    void deleteCommentLikesByCommentId(Long commentId);
}
