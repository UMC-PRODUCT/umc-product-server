package com.umc.product.techblog.application.port.out;

import com.umc.product.techblog.domain.TechBlogComment;

public interface SaveTechBlogCommentPort {

    TechBlogComment save(TechBlogComment comment);

    TechBlogComment updateContent(Long commentId, String content);

    TechBlogComment softDelete(Long commentId, Long deletedByMemberId, boolean admin);

    void hardDelete(Long commentId);
}
