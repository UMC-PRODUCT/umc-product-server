package com.umc.product.blog.application.port.in.query;

import com.umc.product.blog.application.port.in.query.dto.BlogCommentCursorInfo;
import com.umc.product.blog.application.port.in.query.dto.BlogCommentListQuery;

public interface GetBlogCommentListUseCase {

    BlogCommentCursorInfo getComments(BlogCommentListQuery query);
}
