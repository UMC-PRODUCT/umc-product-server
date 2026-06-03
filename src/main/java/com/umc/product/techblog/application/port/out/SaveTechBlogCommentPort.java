package com.umc.product.techblog.application.port.out;

import com.umc.product.techblog.domain.TechBlogComment;

public interface SaveTechBlogCommentPort {

    TechBlogComment save(TechBlogComment comment);

    void hardDelete(Long commentId);
}
