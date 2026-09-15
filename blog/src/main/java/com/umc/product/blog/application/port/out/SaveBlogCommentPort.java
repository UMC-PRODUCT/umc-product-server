package com.umc.product.blog.application.port.out;

import com.umc.product.blog.domain.BlogComment;

public interface SaveBlogCommentPort {

    BlogComment save(BlogComment comment);

    void hardDelete(Long commentId);
}
